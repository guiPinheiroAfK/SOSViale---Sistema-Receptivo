package br.com.sosviale.util.pathfinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Grava o resultado de cada execução do PathFinding em um arquivo de log rotativo.
 *
 * ─── Formato do arquivo ───────────────────────────────────────────────────────
 *   logs/pathfinding/pathfinding_2026-04-27.log
 *
 *   Um arquivo por dia. Múltiplas execuções no mesmo dia são ACUMULADAS
 *   (append), não sobrescritas — cada bloco é separado por linha divisória.
 *
 * ─── Por que .log e não .txt ou .dat? ────────────────────────────────────────
 *   .log é o formato padrão da indústria para arquivos de diagnóstico.
 *   É texto puro (leve, legível no bloco de notas), mas sinaliza claramente
 *   que é gerado pelo sistema — não um documento para editar.
 *   IDEs como IntelliJ têm syntax highlight nativo para .log.
 *
 * ─── Localização ─────────────────────────────────────────────────────────────
 *   A pasta "logs/pathfinding/" é criada automaticamente se não existir,
 *   relativa ao diretório de trabalho da aplicação (onde o .jar é executado).
 */
public final class RouteLogger {

    private static final Logger JAVA_LOG = Logger.getLogger(RouteLogger.class.getName());

    private static final String LOG_DIR        = "logs/pathfinding";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private RouteLogger() {}

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /**
     * Grava o resultado de uma otimização no arquivo de log do dia.
     *
     * @param osId    ID da Ordem de Serviço otimizada (para rastreabilidade)
     * @param resultado  resultado produzido pelo {@link RouteOptimizer}
     */
    public static void gravar(Integer osId, RouteResult resultado) {
        try {
            Path dir  = Paths.get(LOG_DIR);
            Files.createDirectories(dir);

            String nomeArquivo = "pathfinding_" + LocalDateTime.now().format(DATE_FMT) + ".log";
            Path   arquivo     = dir.resolve(nomeArquivo);

            String conteudo = montarBloco(osId, resultado);

            Files.writeString(arquivo, conteudo,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            JAVA_LOG.info("Log de rota gravado em: " + arquivo.toAbsolutePath());

        } catch (IOException e) {
            // Falha de log nunca deve interromper a operação principal
            JAVA_LOG.warning("Nao foi possivel gravar o log de rota: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Construção do bloco de texto
    // -------------------------------------------------------------------------

    private static String montarBloco(Integer osId, RouteResult resultado) {
        LocalDateTime agora = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("  PATHFINDING  |  OS #").append(osId == null ? "TESTE" : osId)
          .append("  |  ").append(agora.format(DATE_FMT))
          .append(" ").append(agora.format(TIME_FMT)).append("\n");
        sb.append("  Motor: ").append(resultado.getModoCalculo().descricao).append("\n");
        sb.append("═══════════════════════════════════════════════════════════\n");

        sb.append("\n  SEQUÊNCIA DE PARADAS:\n");
        sb.append("  ─────────────────────\n");

        for (int i = 0; i < resultado.getRotaOtimizada().size(); i++) {
            Coordenada c = resultado.getRotaOtimizada().get(i);
            sb.append(String.format("  [%2d] %-40s  (%.6f, %.6f)%n",
                    i + 1,
                    c.getNome(),
                    c.getLatitude(),
                    c.getLongitude()));
        }

        sb.append("\n  DECISÕES DO ALGORITMO:\n");
        sb.append("  ──────────────────────\n");
        resultado.getLogDecisoes().forEach(linha ->
                sb.append("  ").append(linha).append("\n"));

        sb.append("\n  ──────────────────────────────────────────────────────\n");
        sb.append(String.format("  DISTÂNCIA TOTAL ESTIMADA: %.2f km%n",
                resultado.getDistanciaTotalKm()));
        sb.append("═══════════════════════════════════════════════════════════\n\n");

        return sb.toString();
    }
}
