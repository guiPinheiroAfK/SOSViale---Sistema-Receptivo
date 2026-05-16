package br.com.sosviale.service.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

// implementa o algoritmo de otimização de rota por Nearest Neighbor (Vizinho Mais Próximo).

public final class RouteOptimizer {

    private RouteOptimizer() {}

    // Otimiza a ordem de visita dos pontos usando o motor de distância informado.
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

        // ponto de partida: GPS do motorista se disponível, senão o primeiro da lista
        Coordenada atual = temPosicaoMotorista ? pontoDePartida : pontos.get(0);

        List<Coordenada> naoVisitados = new ArrayList<>(pontos);
        List<Coordenada> rotaOtimizada = new ArrayList<>();
        List<String>     log           = new ArrayList<>();
        double           distanciaTotal = 0.0;

        log.add("Partindo de: " + atual.getNome());

        //Nearest Neighbor greedy
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

        // determina o modo para o resultado
        RouteResult.ModoCalculo modo = resolverModo(usarOsrm, temPosicaoMotorista);

        return new RouteResult(rotaOtimizada, distanciaTotal, log, modo);
    }

    // Helpers privados
     // varre a lista de pontos não visitados e retorna o mais próximo do ponto atual.

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
