package br.com.sosviale.service.pathfinding;

import java.time.LocalTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.logging.Logger;

// ordena pontos por horário (prioridade absoluta)
// depois tenta construir rota greedy respeitando order
// aí se houver conflitos (impossível chegar no horário), marca como INVÁLIDO
// o admin recebe aviso e pode mover alguns transfers para próxima OS, aumentar margem de tempo ou ignorar conflito (motorista chegará atrasado com justificativa)
public final class ConstraintAwareRouteOptimizer {

    private static final Logger LOG = Logger.getLogger(ConstraintAwareRouteOptimizer.class.getName());

    private ConstraintAwareRouteOptimizer() {}
    public static RouteResult otimizarComTimeWindows(
            List<TimeWindowCoordenada> pontos,
            TimeWindowCoordenada pontoDePartida,
            boolean usarOsrm,
            BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Integer> tempoViagemFn) {

        if (pontos == null || pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto para otimizar.");
        }

        // função de distância
        BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Double> distFn = usarOsrm
                ? (o, d) -> (double) DistanceCalculator.osrm(o, d)
                : (o, d) -> (double) DistanceCalculator.haversine(o, d);

        // ponto de partida: posição do motorista ou primeiro ponto
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
            // filtra candidatos viáveis (que conseguem ser visitados no horário)
            TimeWindowCoordenada finalAtual = atual;
            LocalTime finalHoraAtual = horaAtual;
            List<TimeWindowCoordenada> candViaveis = naoVisitados.stream()
                    .filter(cand -> isChegadaViavel(finalAtual, finalHoraAtual, tempoViagemFn, cand))
                    .toList();

            TimeWindowCoordenada proximo;

            if (candViaveis.isEmpty()) {
                // escolhe o que tem menor janela de tempo (mais urgente)
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
                // escolhe o candidato viável mais próximo
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

                // atualiza hora atual: máximo entre (hora chegada) e (hora prevista - para esperar se necessário)
                if (proximo.getHorarioPrevisto() != null && proximo.getHorarioPrevisto().isAfter(chegada)) {
                    // chega cedo — espera até o horário previsto
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

        // determina modo
        RouteResult.ModoCalculo modo = resolverModo(usarOsrm, pontoDePartida != null);

        // adiciona conflitos ao log se houver
        if (!conflitos.isEmpty()) {
            log.add("");
            log.add("⚠ AVISOS DE CONFLITO:");
            log.addAll(conflitos);
        }

        return new RouteResult(rotaOtimizada, distanciaTotal, log, modo);
    }

    // helpers privados
    //verifica se é viável chegar a um ponto no horário esperado
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
