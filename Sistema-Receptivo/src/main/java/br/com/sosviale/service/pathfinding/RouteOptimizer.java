package br.com.sosviale.service.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/*
 * Implementa o algoritmo de otimização de rota por Nearest Neighbor (Vizinho Mais Próximo).
 *
 * ─── Como funciona ────────────────────────────────────────────────────────────
 *
 *   Dado o problema do exemplo:
 *
 *     Passageiro no Ponto A  (ex: Aeroporto — lon -54.49, lat -25.59)
 *     Passageiro no Ponto B  (ex: Hotel Centro — lon -54.57, lat -25.52)
 *     Destino perto do Ponto B
 *     Motorista está perto do Ponto A
 *
 *   Ordem ORIGINAL (manual, ruim):  Motorista → B → A → B (retorno desnecessário!)
 *   Ordem OTIMIZADA (algoritmo):    Motorista → A → B       (sem retorno)
 *
 *   O algoritmo sempre escolhe o próximo ponto NÃO visitado mais próximo
 *   do ponto atual. Isso elimina zigue-zagues e retornos óbvios.
 *
 * ─── Complexidade ─────────────────────────────────────────────────────────────
 *
 *   O(n²) onde n = número de pontos de coleta.
 *   Para o contexto do sistema (rotas urbanas com 2–20 paradas), é ideal.
 *   Para n > 50, considerar algoritmos mais sofisticados (2-opt, Lin–Kernighan).
 *
 * ─── Modos de operação ───────────────────────────────────────────────────────
 *
 *   Sem posição do motorista → ponto de partida = primeiro ponto da lista original
 *   Com posição do motorista → ponto de partida = coordenadas GPS do motorista
 *   Motor OSRM ativado       → distâncias reais de estrada em vez de linha reta
 */
public final class RouteOptimizer {

    private RouteOptimizer() {}

    /*
     * Otimiza a ordem de visita dos pontos usando o motor de distância informado.
     *
     * @param pontos          lista de pontos a visitar (em qualquer ordem)
     * @param pontoDePartida  posição inicial do motorista, ou null para usar o primeiro ponto
     * @param usarOsrm        true = distâncias reais via OSRM; false = Haversine
     * @return resultado com rota otimizada, distância total e log de decisões
     */
    public static RouteResult otimizar(List<Coordenada> pontos,
                                       Coordenada pontoDePartida,
                                       boolean usarOsrm) {

        if (pontos == null || pontos.isEmpty()) {
            return new RouteResult(List.of(), 0.0, List.of("Nenhum ponto para otimizar."),
                    RouteResult.ModoCalculo.HAVERSINE);
        }

        // Escolhe a função de distância conforme o modo configurado
        BiFunction<Coordenada, Coordenada, Double> distFn = usarOsrm
                ? DistanceCalculator::osrm
                : DistanceCalculator::haversine;

        boolean temPosicaoMotorista = pontoDePartida != null;

        // Ponto de partida: GPS do motorista se disponível, senão o primeiro da lista
        Coordenada atual = temPosicaoMotorista ? pontoDePartida : pontos.get(0);

        List<Coordenada> naoVisitados = new ArrayList<>(pontos);
        List<Coordenada> rotaOtimizada = new ArrayList<>();
        List<String>     log           = new ArrayList<>();
        double           distanciaTotal = 0.0;

        log.add("Partindo de: " + atual.getNome());

        // ── Nearest Neighbor greedy ───────────────────────────────────────────
        while (!naoVisitados.isEmpty()) {
            Coordenada maisProximo = encontrarMaisProximo(atual, naoVisitados, distFn);
            double distTrecho = distFn.apply(atual, maisProximo);

            log.add(String.format("Passo %d: %s  →  %s  |  %.2f km",
                    rotaOtimizada.size() + 1,
                    atual.getNome(),
                    maisProximo.getNome(),
                    distTrecho));

            distanciaTotal += distTrecho;
            rotaOtimizada.add(maisProximo);
            naoVisitados.remove(maisProximo);
            atual = maisProximo;
        }

        // Determina o modo para o resultado
        RouteResult.ModoCalculo modo = resolverModo(usarOsrm, temPosicaoMotorista);

        return new RouteResult(rotaOtimizada, distanciaTotal, log, modo);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /*
     * Varre a lista de pontos não visitados e retorna o mais próximo do ponto atual.
     */
    private static Coordenada encontrarMaisProximo(Coordenada atual,
                                                    List<Coordenada> candidatos,
                                                    BiFunction<Coordenada, Coordenada, Double> distFn) {
        Coordenada melhor       = null;
        double     menorDistancia = Double.MAX_VALUE;

        for (Coordenada candidato : candidatos) {
            double d = distFn.apply(atual, candidato);
            if (d < menorDistancia) {
                menorDistancia = d;
                melhor = candidato;
            }
        }

        return melhor; // nunca null porque candidatos não está vazio
    }

    private static RouteResult.ModoCalculo resolverModo(boolean osrm, boolean comPosicao) {
        if (osrm  && comPosicao) return RouteResult.ModoCalculo.OSRM_COM_POSICAO;
        if (osrm)                return RouteResult.ModoCalculo.OSRM;
        if (comPosicao)          return RouteResult.ModoCalculo.HAVERSINE_COM_POSICAO;
        return                          RouteResult.ModoCalculo.HAVERSINE;
    }
}
