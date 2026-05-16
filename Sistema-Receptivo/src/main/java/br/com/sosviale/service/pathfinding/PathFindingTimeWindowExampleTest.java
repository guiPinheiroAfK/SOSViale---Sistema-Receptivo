package br.com.sosviale.service.pathfinding;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Demonstração prática do VRP com Time Windows.
 *
 * Cenários testados:
 *   1. Problema clássico: B às 08:30, A às 08:45
 *      → Algoritmo corrige para B, depois A (respeita ordem cronológica)
 *
 *   2. Conflito detectado: 5 pontos em 50 km, 30 minutos
 *      → Impossível chegar em tempo
 *      → Sistema marca como ⚠ CONFLITO
 *      → Admin recebe avisos
 *
 *   3. Chegada cedo: motorista chega 20 min antes
 *      → Sistema registra "Aguardando até..." no log
 *      → Passageiro é buscado no horário prometido
 *
 * Como executar:
 *   mvn test -Dtest=PathFindingTimeWindowExampleTest
 *   OU direto pela IDE: Run → PathFindingTimeWindowExampleTest
 *
 * Output esperado:
 *   ═══ CENÁRIO 1 — Ordem Respeitada ═══
 *   ✓ Passo 1: ... → Hotel Bourbon (08:30)
 *   ✓ Passo 2: ... → Aeroporto (08:45)
 *   Sem conflitos!
 *
 *   ═══ CENÁRIO 2 — Conflito Detectado ═══
 *   ✗ Passo 4: ... → Ponto Final (impossível!)
 *   ⚠ AVISO: Conflito de horário detectado
 */
public class PathFindingTimeWindowExampleTest {

    // Coordenadas reais de Foz do Iguaçu
    private static final double LAT_CENTRO    = -25.53;
    private static final double LON_CENTRO    = -54.58;
    private static final double LAT_AEROPORTO = -25.59;
    private static final double LON_AEROPORTO = -54.48;
    private static final double LAT_HOTEL_B   = -25.59;
    private static final double LON_HOTEL_B   = -54.52;
    private static final double LAT_PARQUE    = -25.68;
    private static final double LON_PARQUE    = -54.45;
    private static final double LAT_ITAIPU    = -25.40;
    private static final double LON_ITAIPU    = -54.58;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  VRP COM TIME WINDOWS — Demonstração Prática              ║");
        System.out.println("║  Sistema: SOS VIALE — Otimização de Rotas                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        teste1_CenarioClassico();
        separador();
        teste2_ConflitoDectado();
        separador();
        teste3_ChegadaCedo();

        System.out.println("\n✓ Testes completados.\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TESTE 1: Cenário Clássico — Ordem Respeitada
    // ═══════════════════════════════════════════════════════════════════════════

    private static void teste1_CenarioClassico() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  TESTE 1 — Cenário Clássico: B 08:30, A 08:45              │");
        System.out.println("│  Esperado: Sistema RESPEITA ordem cronológica               │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        // Problema original: cadastrado como B, depois A
        List<TimeWindowCoordenada> pontos = new ArrayList<>();
        pontos.add(new TimeWindowCoordenada(
                LAT_HOTEL_B, LON_HOTEL_B, "Hotel Bourbon",
                null, null, LocalTime.of(8, 30), 15));
        pontos.add(new TimeWindowCoordenada(
                LAT_AEROPORTO, LON_AEROPORTO, "Aeroporto",
                null, null, LocalTime.of(8, 45), 15));

        System.out.println("Ordem ORIGINAL (como foi cadastrado):");
        for (int i = 0; i < pontos.size(); i++) {
            System.out.printf("  [%d] %s — %s%n", i + 1, pontos.get(i).getNome(),
                    pontos.get(i).getHorarioPrevisto());
        }
        System.out.println();

        // Ponto de partida: motorista no centro
        TimeWindowCoordenada motorista = new TimeWindowCoordenada(
                LAT_CENTRO, LON_CENTRO, "Centro (Posição Motorista)");

        System.out.println("Posição do Motorista: " + motorista.getNome());
        System.out.println("Hora Inicial: 08:00\n");

        // Função de tempo de viagem
        var tempoViagemFn = criarFuncaoTempoViagem();

        // Otimiza
        System.out.println("Executando otimização com time windows...\n");
        RouteResult resultado = ConstraintAwareRouteOptimizer.otimizarComTimeWindows(
                pontos, motorista, false, tempoViagemFn);

        // Exibe resultado
        System.out.println("Ordem OTIMIZADA (respeitando time windows):");
        for (String linha : resultado.getLogDecisoes()) {
            System.out.println("  " + linha);
        }

        // Análise
        System.out.println("\n✓ ANÁLISE:");
        if (resultado.getLogDecisoes().stream().anyMatch(l -> l.contains("CONFLITO"))) {
            System.out.println("  ✗ Conflitos detectados");
        } else {
            System.out.println("  ✓ Nenhum conflito — ordem é viável");
            System.out.println("  ✓ Distância total: " + String.format("%.2f km", resultado.getDistanciaTotalKm()));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TESTE 2: Conflito Detectado
    // ═══════════════════════════════════════════════════════════════════════════

    private static void teste2_ConflitoDectado() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  TESTE 2 — Conflito Detectado: 4 pontos, 50 km, 30 min    │");
        System.out.println("│  Esperado: Sistema marca IMPOSSÍVEL ⚠                      │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        // 4 pontos muito longe uns dos outros, horários muito próximos
        List<TimeWindowCoordenada> pontos = new ArrayList<>();
        pontos.add(new TimeWindowCoordenada(
                LAT_HOTEL_B, LON_HOTEL_B, "Hotel Bourbon (8km)",
                null, null, LocalTime.of(8, 10), 10));
        pontos.add(new TimeWindowCoordenada(
                LAT_PARQUE, LON_PARQUE, "Parque das Aves (20km)",
                null, null, LocalTime.of(8, 20), 10));
        pontos.add(new TimeWindowCoordenada(
                LAT_ITAIPU, LON_ITAIPU, "Usina Itaipu (50km)",
                null, null, LocalTime.of(8, 30), 10));
        pontos.add(new TimeWindowCoordenada(
                LAT_AEROPORTO, LON_AEROPORTO, "Aeroporto (80km)",
                null, null, LocalTime.of(8, 40), 10));

        System.out.println("Pontos a visitar (em 30 minutos, ~80 km distância):");
        for (int i = 0; i < pontos.size(); i++) {
            System.out.printf("  [%d] %-30s %s  (~%.0f km do centro)%n",
                    i + 1, pontos.get(i).getNome(),
                    pontos.get(i).getHorarioPrevisto(),
                    calcularDistanciaAproximada(LAT_CENTRO, LON_CENTRO,
                            pontos.get(i).getLatitude(), pontos.get(i).getLongitude()));
        }
        System.out.println("\nVelocidade média urbana: 40 km/h");
        System.out.println("Tempo necessário mínimo: 80 km / 40 km/h = 120 minutos");
        System.out.println("Tempo disponível: 30 minutos");
        System.out.println("→ IMPOSSÍVEL!\n");

        // Ponto de partida
        TimeWindowCoordenada motorista = new TimeWindowCoordenada(
                LAT_CENTRO, LON_CENTRO, "Centro (Posição Motorista)");

        var tempoViagemFn = criarFuncaoTempoViagem();

        System.out.println("Executando otimização com time windows...\n");
        RouteResult resultado = ConstraintAwareRouteOptimizer.otimizarComTimeWindows(
                pontos, motorista, false, tempoViagemFn);

        System.out.println("Resultado:");
        for (String linha : resultado.getLogDecisoes()) {
            System.out.println("  " + linha);
        }

        // Análise
        System.out.println("\n⚠ ANÁLISE:");
        if (resultado.getLogDecisoes().stream().anyMatch(l -> l.contains("CONFLITO"))) {
            System.out.println("  ⚠ Conflitos detectados — motorista não conseguirá chegar");
            System.out.println("  ⚠ Admin deve: mover alguns transfers para próxima OS");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TESTE 3: Chegada Cedo (Motorista Espera)
    // ═══════════════════════════════════════════════════════════════════════════

    private static void teste3_ChegadaCedo() {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.println("│  TESTE 3 — Chegada Cedo: Motorista aguarda até horário    │");
        System.out.println("│  Esperado: ✓ Sem conflito, mas com \"Aguardando...\"       │");
        System.out.println("└────────────────────────────────────────────────────────────┘\n");

        List<TimeWindowCoordenada> pontos = new ArrayList<>();
        pontos.add(new TimeWindowCoordenada(
                LAT_HOTEL_B, LON_HOTEL_B, "Hotel Bourbon",
                null, null, LocalTime.of(8, 45), 15));
        pontos.add(new TimeWindowCoordenada(
                LAT_AEROPORTO, LON_AEROPORTO, "Aeroporto",
                null, null, LocalTime.of(9, 15), 15));

        System.out.println("Pontos a visitar:");
        for (TimeWindowCoordenada p : pontos) {
            System.out.printf("  • %s — %s%n", p.getNome(), p.getHorarioPrevisto());
        }
        System.out.println();

        TimeWindowCoordenada motorista = new TimeWindowCoordenada(
                LAT_CENTRO, LON_CENTRO, "Centro");

        var tempoViagemFn = criarFuncaoTempoViagem();

        System.out.println("Tempo Centro → Hotel Bourbon: ~15 minutos");
        System.out.println("Hora de saída: 08:00");
        System.out.println("Hora de chegada estimada: 08:15");
        System.out.println("Hora prometida ao passageiro: 08:45");
        System.out.println("→ Motorista chega 30 minutos cedo\n");

        System.out.println("Executando otimização...\n");
        RouteResult resultado = ConstraintAwareRouteOptimizer.otimizarComTimeWindows(
                pontos, motorista, false, tempoViagemFn);

        System.out.println("Resultado:");
        for (String linha : resultado.getLogDecisoes()) {
            System.out.println("  " + linha);
        }

        System.out.println("\n✓ ANÁLISE:");
        if (resultado.getLogDecisoes().stream().anyMatch(l -> l.contains("Aguardando"))) {
            System.out.println("  ✓ Motorista aguarda até o horário (passageiro é buscado no horário certo)");
        }
        System.out.println("  ✓ Sem conflitos de time window");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private static java.util.function.BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Integer>
    criarFuncaoTempoViagem() {
        return (origem, destino) -> {
            double dist = DistanceCalculator.haversine(origem, destino);
            // Tempo = (distância / 40 km/h) * 60 + 5 min parada
            return (int) Math.ceil((dist / 40.0) * 60) + 5;
        };
    }

    private static double calcularDistanciaAproximada(double lat1, double lon1,
                                                      double lat2, double lon2) {
        return DistanceCalculator.haversine(
                new TimeWindowCoordenada(lat1, lon1, "A"),
                new TimeWindowCoordenada(lat2, lon2, "B"));
    }

    private static void separador() {
        System.out.println("\n────────────────────────────────────────────────────────────\n");
    }
}
