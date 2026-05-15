package br.com.sosviale.service.pathfinding;

import java.time.LocalTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/* Fluxo
 *
 *   1. Ordena pontos por horário (prioridade absoluta)
 *   2. Tenta construir rota greedy respeitando order
 *   3. Se houver conflitos (impossível chegar no horário), marca como INVÁLIDO
 *   4. Admin recebe aviso e pode:
 *      a) Mover alguns transfers para próxima OS
 *      b) Aumentar margem de tempo
 *      c) Ignorar conflito (motorista chegará atrasado com justificativa)
 */
public final class ConstraintAwareRouteOptimizer {

    private static final Logger LOG = Logger.getLogger(ConstraintAwareRouteOptimizer.class.getName());

    private ConstraintAwareRouteOptimizer() {}
    /* API Pública
     *
     * @param pontos         lista de TimeWindowCoordenada com horários
     * @param pontoDePartida posição inicial do motorista (pode ser null)
     * @param usarOsrm       true para distâncias reais, false para Haversine
     * @param tempoViagemFn  função que calcula tempo de viagem em minutos
     * @return resultado com rota otimizada, conflitos detectados e log
     */
    public static RouteResult otimizarComTimeWindows(
            List<TimeWindowCoordenada> pontos,
            TimeWindowCoordenada pontoDePartida,
            boolean usarOsrm,
            BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Integer> tempoViagemFn) {

        if (pontos == null || pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto para otimizar.");
        }

        // Função de distância
        BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Double> distFn = usarOsrm
                ? (o, d) -> (double) DistanceCalculator.osrm(o, d)
                : (o, d) -> (double) DistanceCalculator.haversine(o, d);

        // Ponto de partida: posição do motorista ou primeiro ponto
        TimeWindowCoordenada atual = pontoDePartida != null ? pontoDePartida : pontos.get(0);
        LocalTime horaAtual = LocalTime.now();

        List<TimeWindowCoordenada> naoVisitados = new ArrayList<>(pontos);
        if (pontoDePartida != null) {
            naoVisitados.removeIf(p -> p == pontoDePartida);
        } else {
            naoVisitados.remove(0); // Remove o primeiro (ponto de partida)
        }

        List<Coordenada> rotaOtimizada = new ArrayList<>();
        List<String> log = new ArrayList<>();
        List<String> conflitos = new ArrayList<>();
        double distanciaTotal = 0.0;

        log.add("Partindo de: " + atual.getNome() + " às " + horaAtual.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));


        while (!naoVisitados.isEmpty()) {
            // Filtra candidatos viáveis (que conseguem ser visitados no horário)
            TimeWindowCoordenada finalAtual = atual;
            LocalTime finalHoraAtual = horaAtual;
            List<TimeWindowCoordenada> candViaveis = naoVisitados.stream()
                    .filter(cand -> isChegadaViavel(finalAtual, finalHoraAtual, tempoViagemFn, cand))
                    .toList();

            TimeWindowCoordenada proximo;

            if (candViaveis.isEmpty()) {
                // CONFLITO: nenhum ponto é viável para o horário estimado
                // Escolhe o que tem menor janela de tempo (mais urgente)
                proximo = naoVisitados.stream()
                        .min(Comparator.comparing(TimeWindowCoordenada::getHorarioPrevisto,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(null);

                if (proximo != null) {
                    int tempoViagem = tempoViagemFn.apply(atual, proximo);
                    LocalTime chegada = horaAtual.plusMinutes(tempoViagem);

                    conflitos.add(String.format(
                            "⚠ CONFLITO: %s (esperado às %s) — chegada prevista às %s",
                            proximo.getNome(),
                            proximo.getHorarioPrevisto().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                            chegada.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                    ));
                }
            } else {
                // Escolhe o candidato viável mais próximo (Nearest Neighbor com constraints)
                TimeWindowCoordenada finalAtual1 = atual;
                proximo = candViaveis.stream()
                        .min(Comparator.comparingDouble(c -> distFn.apply(finalAtual1, c)))
                        .orElse(candViaveis.get(0));
            }

            if (proximo != null) {
                int tempoViagem = tempoViagemFn.apply(atual, proximo);
                LocalTime chegada = horaAtual.plusMinutes(tempoViagem);
                double distTrecho = distFn.apply(atual, proximo);

                String status = proximo.isChegadaViavel(chegada) ? "✓" : "✗";
                log.add(String.format(
                        "Passo %d: %s → %s | %s | chegada: %s (esperado: %s) | %.2f km",
                        rotaOtimizada.size() + 1,
                        atual.getNome(),
                        proximo.getNome(),
                        status,
                        chegada.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                        proximo.getHorarioPrevisto() != null
                                ? proximo.getHorarioPrevisto().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                : "—",
                        distTrecho
                ));

                distanciaTotal += distTrecho;
                rotaOtimizada.add(proximo);
                naoVisitados.remove(proximo);

                // Atualiza hora atual: máximo entre (hora chegada) e (hora prevista - para esperar se necessário)
                if (proximo.getHorarioPrevisto() != null && proximo.getHorarioPrevisto().isAfter(chegada)) {
                    // Chega cedo — espera até o horário previsto
                    horaAtual = proximo.getHorarioPrevisto();
                    log.add("   └─ Aguardando até " + horaAtual.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    horaAtual = chegada;
                }

                atual = proximo;
            } else {
                break;
            }
        }

        // Determina modo
        RouteResult.ModoCalculo modo = resolverModo(usarOsrm, pontoDePartida != null);

        // Adiciona conflitos ao log se houver
        if (!conflitos.isEmpty()) {
            log.add("");
            log.add("⚠ AVISOS DE CONFLITO:");
            log.addAll(conflitos);
        }

        return new RouteResult(rotaOtimizada, distanciaTotal, log, modo);
    }

    // Helpers privados


    //Verifica se é viável chegar a um ponto no horário esperado
    private static boolean isChegadaViavel(
            TimeWindowCoordenada atual,
            LocalTime horaAtual,
            BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Integer> tempoViagemFn,
            TimeWindowCoordenada candidato) {

        int tempoViagem = tempoViagemFn.apply(atual, candidato);
        LocalTime horaChegada = horaAtual.plusMinutes(tempoViagem);

        return candidato.isChegadaViavel(horaChegada);
    }

    private static RouteResult.ModoCalculo resolverModo(boolean osrm, boolean comPosicao) {
        if (osrm && comPosicao) return RouteResult.ModoCalculo.OSRM_COM_POSICAO;
        if (osrm) return RouteResult.ModoCalculo.OSRM;
        if (comPosicao) return RouteResult.ModoCalculo.HAVERSINE_COM_POSICAO;
        return RouteResult.ModoCalculo.HAVERSINE;
    }

    private static RouteResult resultadoVazio(String motivo) {
        LOG.warning("ConstraintAwareRouteOptimizer: " + motivo);
        return new RouteResult(
                List.of(),
                0.0,
                List.of("[AVISO] " + motivo),
                RouteResult.ModoCalculo.HAVERSINE
        );
    }
}
