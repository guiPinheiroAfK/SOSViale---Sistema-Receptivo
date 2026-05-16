package br.com.sosviale.service.pathfinding;

import br.com.sosviale.model.*;
import br.com.sosviale.service.PathFinding;
import br.com.sosviale.service.StatusTransfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/*
 * Teste manual do PathFinding com localizações reais de Foz do Iguaçu.
 *
 * ─── Como executar ────────────────────────────────────────────────────────────
 *   Rode o main() diretamente pela IDE (IntelliJ: clique em Run ao lado do metodo).
 *   Não requer banco de dados — todos os objetos são montados em memória.
 *   Um arquivo de log será gerado automaticamente em:
 *     logs/pathfinding/pathfinding_YYYY-MM-DD.log
 *
 * ─── Destinos fixos (conforme especificado) ───────────────────────────────────
 *   ID 1 — Cataratas do Iguaçu
 *   ID 2 — Marco das Três Fronteiras
 *
 * ─── Pontos de coleta (hotéis reais de Foz do Iguaçu) ────────────────────────
 *   Todos com coordenadas reais — nenhuma chamada ao Nominatim será feita.
 *
 * ─── Cenários testados ────────────────────────────────────────────────────────
 *   CENÁRIO 1 — Problema clássico do retorno desnecessário
 *     Motorista próximo ao Aeroporto. OS original mandaria: Hotel Sul → Aeroporto → Hotel Sul.
 *     PathFinding deve corrigir para: Aeroporto → Hotel Sul.
 *
 *   CENÁRIO 2 — Rota com 4 hotéis dispersos na cidade
 *     Valida que o algoritmo cria a rota mais lógica entre pontos espalhados.
 *
 *   CENÁRIO 3 — Modo GPS ativado (posição do motorista como ponto de partida)
 *     Motorista está no Centro. Deve começar pelo ponto mais próximo do Centro.
 *
 *   CENÁRIO 4 — OS com ponto único (rota trivial, sem otimização possível)
 */
public class PathFindingTest {

    // =========================================================================
    // Coordenadas reais de Foz do Iguaçu (fonte: Google Maps / OpenStreetMap)
    // =========================================================================

    // ── Destinos fixos ───────────────────────────────────────────────────────
    private static final double LAT_CATARATAS   = -25.6953;
    private static final double LON_CATARATAS   = -54.4367;
    private static final String NOME_CATARATAS  = "Cataratas do Iguaçu";   // Destino ID 1

    private static final double LAT_MARCO       = -25.5910;
    private static final double LON_MARCO       = -54.5872;
    private static final String NOME_MARCO      = "Marco das Três Fronteiras"; // Destino ID 2

    // ── Pontos de coleta (hotéis / pontos turísticos reais) ──────────────────
    private static final double LAT_AEROPORTO   = -25.5963;
    private static final double LON_AEROPORTO   = -54.4870;
    private static final String NOME_AEROPORTO  = "Aeroporto Internacional de Foz do Iguaçu";

    private static final double LAT_HOTEL_BOURBON  = -25.5921;
    private static final double LON_HOTEL_BOURBON  = -54.5278;
    private static final String NOME_HOTEL_BOURBON = "Hotel Bourbon Cataratas";

    private static final double LAT_HOTEL_RAFAIN   = -25.5695;
    private static final double LON_HOTEL_RAFAIN   = -54.5483;
    private static final String NOME_HOTEL_RAFAIN  = "Hotel Rafain Centro";

    private static final double LAT_HOTEL_BELLA    = -25.5499;
    private static final double LON_HOTEL_BELLA    = -54.5838;
    private static final String NOME_HOTEL_BELLA   = "Hotel Bella Italia";

    private static final double LAT_PARQUE_AVES    = -25.6803;
    private static final double LON_PARQUE_AVES    = -54.4582;
    private static final String NOME_PARQUE_AVES   = "Parque das Aves";

    private static final double LAT_ITAIPU         = -25.4083;
    private static final double LON_ITAIPU         = -54.5892;
    private static final String NOME_ITAIPU        = "Usina de Itaipu";

    // ── Posição de motoristas para o cenário GPS ─────────────────────────────
    private static final double LAT_MOTORISTA_CENTRO = -25.5279;
    private static final double LON_MOTORISTA_CENTRO = -54.5882;
    private static final String NOME_MOTORISTA       = "Posição do Motorista (Centro)";

    // =========================================================================
    // Main
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   SOS VIALE — TESTE DE PATHFINDING                      ║");
        System.out.println("║   Localização: Foz do Iguaçu / Tríplice Fronteira       ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        testarCenario1_ProblemaClassicoRetorno();
        separador();
        testarCenario2_QuatroHoteisDispersos();
        separador();
        testarCenario3_ModoGpsAtivado();
        separador();
        testarCenario4_PontoUnico();

        System.out.println("\n✔  Todos os cenários executados.");
        System.out.println("   Verifique o arquivo de log em: logs/pathfinding/");
    }

    // =========================================================================
    // CENÁRIO 1 — Problema clássico do retorno desnecessário
    // =========================================================================

    /*
     * Demonstra o problema principal que motivou o PathFinding:
     *
     *   Ordem original (ruim):
     *     Aeroporto  →  Hotel Bourbon (próximo ao Marco, lon -54.52)
     *     Hotel Bourbon  →  Parque das Aves (próximo ao Aeroporto, lon -54.45)
     *     ← motorista teve que "voltar" para a região do Aeroporto
     *
     *   Rota otimizada (esperada):
     *     Aeroporto  →  Parque das Aves  →  Hotel Bourbon
     *     (sem retorno — segue progressivamente para oeste)
     *
     *   Destino: Cataratas do Iguaçu (ID 1)
     */
    private static void testarCenario1_ProblemaClassicoRetorno() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  CENÁRIO 1 — Problema do retorno desnecessário           │");
        System.out.println("│  Destino: Cataratas do Iguaçu (ID 1)                    │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        // Monta a OS com pontos na ORDEM ERRADA (como um atendente desatento faria)
        // Ordem original: Hotel Bourbon primeiro, depois Parque das Aves
        // O algoritmo deve inverter para evitar o zigue-zague
        OrdemServico os = montarOS(
                1,
                NOME_CATARATAS, // destino ID 1
                List.of(
                        ponto(1, NOME_HOTEL_BOURBON, LAT_HOTEL_BOURBON, LON_HOTEL_BOURBON,
                                LocalTime.of(8, 0)),
                        ponto(2, NOME_PARQUE_AVES, LAT_PARQUE_AVES, LON_PARQUE_AVES,
                                LocalTime.of(8, 30)),
                        ponto(3, NOME_AEROPORTO, LAT_AEROPORTO, LON_AEROPORTO,
                                LocalTime.of(9, 0))
                )
        );

        System.out.println("  Ordem ORIGINAL (como foi cadastrado no sistema):");
        imprimirPontosOriginais(os);
        System.out.println();

        RouteResult resultado = PathFinding.otimizar(os);

        System.out.println("  Rota OTIMIZADA pelo PathFinding:");
        imprimirResultado(resultado);
    }

    // =========================================================================
    // CENÁRIO 2 — Quatro hotéis dispersos pela cidade
    // =========================================================================

    /*
     * Quatro pontos de coleta espalhados em Foz do Iguaçu.
     * Destino: Marco das Três Fronteiras (ID 2), no extremo oeste.
     *
     * Rota esperada (do leste para o oeste, sem zigue-zague):
     *   Aeroporto (lon -54.48) → Parque das Aves (-54.45) → Hotel Bourbon (-54.52)
     *   → Hotel Rafain (-54.54) → Hotel Bella Italia (-54.58) → Marco (-54.58)
     */
    private static void testarCenario2_QuatroHoteisDispersos() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  CENÁRIO 2 — Quatro hotéis dispersos na cidade           │");
        System.out.println("│  Destino: Marco das Três Fronteiras (ID 2)               │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        OrdemServico os = montarOS(
                2,
                NOME_MARCO, // destino ID 2
                List.of(
                        // Deliberadamente fora de ordem geográfica para forçar a otimização
                        ponto(1, NOME_HOTEL_BELLA,   LAT_HOTEL_BELLA,   LON_HOTEL_BELLA,   LocalTime.of(7, 0)),
                        ponto(2, NOME_AEROPORTO,     LAT_AEROPORTO,     LON_AEROPORTO,     LocalTime.of(7, 30)),
                        ponto(3, NOME_HOTEL_RAFAIN,  LAT_HOTEL_RAFAIN,  LON_HOTEL_RAFAIN,  LocalTime.of(8, 0)),
                        ponto(4, NOME_HOTEL_BOURBON, LAT_HOTEL_BOURBON, LON_HOTEL_BOURBON, LocalTime.of(8, 30)),
                        ponto(5, NOME_PARQUE_AVES,   LAT_PARQUE_AVES,   LON_PARQUE_AVES,   LocalTime.of(9, 0))
                )
        );

        System.out.println("  Ordem ORIGINAL (propositalmente embaralhada):");
        imprimirPontosOriginais(os);
        System.out.println();

        RouteResult resultado = PathFinding.otimizar(os);

        System.out.println("  Rota OTIMIZADA pelo PathFinding:");
        imprimirResultado(resultado);
    }

    // =========================================================================
    // CENÁRIO 3 — Modo GPS: posição real do motorista como ponto de partida
    // =========================================================================

    /*
     * O motorista está no Centro (perto do Hotel Bella Italia e do Marco).
     * Com GPS ativo, o algoritmo deve começar pelo ponto mais próximo do Centro,
     * não pelo primeiro ponto da lista.
     *
     * Destino: Cataratas do Iguaçu (ID 1).
     */
    private static void testarCenario3_ModoGpsAtivado() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  CENÁRIO 3 — Modo GPS (posição real do motorista)        │");
        System.out.println("│  Motorista: Centro de Foz  |  Destino: Cataratas (ID 1) │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        OrdemServico os = montarOS(
                3,
                NOME_CATARATAS,
                List.of(
                        ponto(1, NOME_ITAIPU,        LAT_ITAIPU,        LON_ITAIPU,        LocalTime.of(6, 0)),
                        ponto(2, NOME_AEROPORTO,     LAT_AEROPORTO,     LON_AEROPORTO,     LocalTime.of(6, 30)),
                        ponto(3, NOME_HOTEL_BOURBON, LAT_HOTEL_BOURBON, LON_HOTEL_BOURBON, LocalTime.of(7, 0)),
                        ponto(4, NOME_HOTEL_BELLA,   LAT_HOTEL_BELLA,   LON_HOTEL_BELLA,   LocalTime.of(7, 30))
                )
        );

        // Define posição GPS do motorista (Centro de Foz)
        os.getMotorista().setLatitudeAtual(LAT_MOTORISTA_CENTRO);
        os.getMotorista().setLongitudeAtual(LON_MOTORISTA_CENTRO);

        System.out.println("  Posição do motorista: " + NOME_MOTORISTA);
        System.out.println("  Ordem ORIGINAL:");
        imprimirPontosOriginais(os);
        System.out.println();

        // Simula o modo GPS sem chamar o banco — usa a posição já definida no motorista
        Coordenada posicaoMotorista = new Coordenada(
                LAT_MOTORISTA_CENTRO, LON_MOTORISTA_CENTRO, NOME_MOTORISTA);

        List<Coordenada> pontos = extrairCoordenadas(os);
        RouteResult resultado = RouteOptimizer.otimizar(pontos, posicaoMotorista, false);
        RouteLogger.gravar(os.getId(), resultado);

        System.out.println("  Rota OTIMIZADA (partindo da posição real do motorista):");
        imprimirResultado(resultado);
    }

    // =========================================================================
    // CENÁRIO 4 — Ponto único (trivial)
    // =========================================================================

    /*
     * Verifica o comportamento com apenas um ponto de coleta.
     * Não há o que otimizar — o algoritmo deve retornar o único ponto sem erro.
     */
    private static void testarCenario4_PontoUnico() {
        System.out.println("┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  CENÁRIO 4 — Ponto único (caso trivial)                  │");
        System.out.println("│  Destino: Marco das Três Fronteiras (ID 2)               │");
        System.out.println("└─────────────────────────────────────────────────────────┘\n");

        OrdemServico os = montarOS(
                4,
                NOME_MARCO,
                List.of(
                        ponto(1, NOME_AEROPORTO, LAT_AEROPORTO, LON_AEROPORTO, LocalTime.of(10, 0))
                )
        );

        RouteResult resultado = PathFinding.otimizar(os);

        System.out.println("  Rota para ponto único:");
        imprimirResultado(resultado);
    }

    // =========================================================================
    // Helpers de construção de objetos em memória
    // =========================================================================

    /*
     * Monta uma OrdemServico em memória com motorista, veículo e um transfer
     * contendo todos os pontos de coleta fornecidos.
     */
    private static OrdemServico montarOS(int osId,
                                          String nomeDestino,
                                          List<PontoColeta> pontosColeta) {
        Motorista motorista = new Motorista("João da Silva", "12345678901");
        motorista.setId(1);  // Integer

        Veiculo veiculo = new Veiculo();
        veiculo.setId(1);     // Integer
        veiculo.setLabel("Mercedes Sprinter");
        veiculo.setPlaca("ABC1D23");
        veiculo.setCapacidade(15);

        Transfer transfer = new Transfer(
                LocalDateTime.now().plusHours(2),
                "Ponto de Partida (Base)",
                nomeDestino,
                new BigDecimal("250.00")
        );
        transfer.setId(osId);   // Integer
        transfer.setStatus(StatusTransfer.AGENDADO);

        // Vincula os pontos ao transfer
        pontosColeta.forEach(pc -> pc.setTransfer(transfer));
        transfer.getPontosColeta().addAll(pontosColeta);

        OrdemServico os = new OrdemServico();
        os.setId(osId);
        os.setDataServico(LocalDate.now());
        os.setMotorista(motorista);
        os.setVeiculo(veiculo);
        os.getTransfers().add(transfer);

        return os;
    }

    /* Cria um PontoColeta com coordenadas reais já preenchidas. */
    private static PontoColeta ponto(int ordem, String nome,
                                      double lat, double lon,
                                      LocalTime horario) {
        PontoColeta pc = new PontoColeta();
        pc.setId(ordem);
        pc.setLocalColeta(nome);
        pc.setOrdemParada(ordem);
        pc.setLatitude(lat);
        pc.setLongitude(lon);
        pc.setHorarioPrevisto(horario);
        return pc;
    }

    /*
     * Extrai coordenadas de uma OS diretamente (usado no cenário GPS,
     * que chama o RouteOptimizer diretamente sem passar pela fachada PathFinding).
     */
    private static List<Coordenada> extrairCoordenadas(OrdemServico os) {
        return os.getTransfers().stream()
                .flatMap(t -> t.getPontosColeta().stream())
                .map(pc -> new Coordenada(pc.getLatitude(), pc.getLongitude(),
                        pc.getLocalColeta(), pc))
                .toList();
    }

    // =========================================================================
    // Helpers de exibição no console
    // =========================================================================

    private static void imprimirPontosOriginais(OrdemServico os) {
        os.getTransfers().stream()
                .flatMap(t -> t.getPontosColeta().stream())
                .forEach(pc -> System.out.printf(
                        "    [%d] %-42s  horário: %s%n",
                        pc.getOrdemParada(),
                        pc.getLocalColeta(),
                        pc.getHorarioPrevisto()));
    }

    private static void imprimirResultado(RouteResult resultado) {
        System.out.println(resultado.resumo());
    }

    private static void separador() {
        System.out.println("───────────────────────────────────────────────────────────\n");
    }
}
