package br.com.sosviale.service.pathfinding;

import java.util.Collections;
import java.util.List;

// Resultado imutável produzido pelo {@link RouteOptimizer}.

public final class RouteResult {

    // ordem de visita otimizada pelo algoritmo.
    private final List<Coordenada> rotaOtimizada;

    //soma das distâncias entre cada par consecutivo na rota (km).
    private final double distanciaTotalKm;
    private final List<String> logDecisoes;

    // motor de distância utilizado nesta otimização
    private final ModoCalculo modoCalculo;

    public RouteResult(List<Coordenada> rotaOtimizada,
                       double distanciaTotalKm,
                       List<String> logDecisoes,
                       ModoCalculo modoCalculo) {
        this.rotaOtimizada    = Collections.unmodifiableList(rotaOtimizada);
        this.distanciaTotalKm = distanciaTotalKm;
        this.logDecisoes      = Collections.unmodifiableList(logDecisoes);
        this.modoCalculo      = modoCalculo;
    }

    public List<Coordenada> getRotaOtimizada()  { return rotaOtimizada;    }
    public double           getDistanciaTotalKm(){ return distanciaTotalKm; }
    public List<String>     getLogDecisoes()     { return logDecisoes;      }
    public ModoCalculo      getModoCalculo()     { return modoCalculo;      }

    //resumo em texto simples para exibição rápida em logs ou console.

    public String resumo() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══ ROTA OTIMIZADA (").append(modoCalculo.descricao).append(") ═══\n");
        logDecisoes.forEach(linha -> sb.append("  ").append(linha).append("\n"));
        sb.append("  ─────────────────────────────────────────\n");
        sb.append(String.format("  Distância total estimada: %.2f km%n", distanciaTotalKm));
        return sb.toString();
    }


    // Enum interno — modo de cálculo de distância
    public enum ModoCalculo {
        HAVERSINE("linha reta — sem GPS do motorista"),
        HAVERSINE_COM_POSICAO("linha reta — com posição GPS do motorista"),
        OSRM("distância real de estrada — OSRM"),
        OSRM_COM_POSICAO("distância real de estrada — OSRM + GPS do motorista");

        public final String descricao;

        ModoCalculo(String descricao) {
            this.descricao = descricao;
        }
    }
}
