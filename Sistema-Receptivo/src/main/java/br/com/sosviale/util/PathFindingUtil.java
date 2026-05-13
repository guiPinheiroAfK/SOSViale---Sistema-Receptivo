package br.com.sosviale.util;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PontoColetaRepository;
import br.com.sosviale.service.pathfinding.Coordenada;
import br.com.sosviale.service.pathfinding.GeocodingService;
import br.com.sosviale.service.pathfinding.RouteLogger;
import br.com.sosviale.service.pathfinding.RouteOptimizer;
import br.com.sosviale.service.pathfinding.RouteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * Utilitário de resolução de rotas (Pathfinding) — camada de infraestrutura reutilizável.
 *
 * Esta classe extrai a lógica bruta do pathfinding do pacote {@code service} e a expõe
 * como uma ferramenta isolada e stateless, sem acoplamento a contextos de negócio específicos.
 * O resultado é livre de dependências cíclicas e pode ser chamado de qualquer camada da aplicação.
 *
 * Diferença em relação ao antigo {@code PathFinding} (service):
 *
 *   Sem dependência direta de {@link MotoristaRepository} — recebe {@link Motorista} já resolvido.
 *   Sem dependência direta de {@link PontoColetaRepository} — persistência é responsabilidade
 *       do chamador (Single Responsibility Principle).
 *   Métodos puramente funcionais: mesma entrada → mesmo resultado (exceto geocodificação externa).
 *
 * Uso típico dentro de OrdemServicoService
 *
 * // Modo básico (Haversine, sem GPS)
 * RouteResult r = PathFindingUtil.otimizar(os);
 *
 * // Modo GPS + OSRM
 * RouteResult r = PathFindingUtil.otimizarComGps(os, motoristaAtualizado);
 *
 * // Aplicar resultado no banco
 * PathFindingUtil.aplicarOrdemOtimizada(r, pcRepo);
 *
 */
public final class PathFindingUtil {

    private static final Logger LOG = Logger.getLogger(PathFindingUtil.class.getName());

    private PathFindingUtil() {}

    // =========================================================================
    // API pública
    // =========================================================================

    /*
     * Modo básico — otimiza a rota de uma OS usando Haversine (linha reta).
     *
     * Não requer internet nem posição GPS do motorista. Adequado para o uso padrão
     * em rotas urbanas de curto/médio alcance.
     *
     * @param os a Ordem de Serviço com transfers e pontos de coleta associados
     * @return resultado com rota otimizada, distância estimada e log de decisões
     */
    public static RouteResult otimizar(OrdemServico os) {
        if (os == null) {
            return resultadoVazio("OS não pode ser nula.");
        }

        List<Coordenada> pontos = coletarCoordenadas(os);

        if (pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto de coleta encontrado na OS #" + os.getId()
                    + ". Cadastre pontos de coleta nos transfers antes de otimizar.");
        }

        LOG.info("PathFindingUtil | Modo básico | OS #" + os.getId()
                + " | " + pontos.size() + " pontos.");

        RouteResult resultado = RouteOptimizer.otimizar(pontos, null, false);
        RouteLogger.gravar(os.getId(), resultado);
        return resultado;
    }

    /*
     * Modo GPS + estrada — otimiza usando a posição real do motorista como ponto
     * de partida e distâncias reais de estrada via OSRM.
     *
     * Se o motorista não tiver GPS cadastrado ou se o OSRM falhar, o metodo
     * faz fallback automático para Haversine sem interromper o fluxo.
     *
     * @param os               a Ordem de Serviço
     * @param motoristaAtualizado o motorista com {@code latitudeAtual}/{@code longitudeAtual} preenchidos
     *                            (deve ser buscado do banco pelo chamador para garantir dados frescos)
     * @return resultado com rota otimizada, distância estimada e log de decisões
     */
    public static RouteResult otimizarComGps(OrdemServico os, Motorista motoristaAtualizado) {
        if (os == null) {
            return resultadoVazio("OS não pode ser nula.");
        }

        List<Coordenada> pontos = coletarCoordenadas(os);

        if (pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto de coleta encontrado na OS #" + os.getId() + ".");
        }

        Coordenada posicaoMotorista = resolverPosicaoMotorista(motoristaAtualizado);

        if (posicaoMotorista == null) {
            LOG.warning("PathFindingUtil | Motorista da OS #" + os.getId()
                    + " sem GPS cadastrado. Otimizando sem ponto de partida físico.");
        } else {
            LOG.info("PathFindingUtil | GPS do motorista: " + posicaoMotorista);
        }

        LOG.info("PathFindingUtil | Modo GPS+OSRM | OS #" + os.getId()
                + " | " + pontos.size() + " pontos.");

        RouteResult resultado = RouteOptimizer.otimizar(pontos, posicaoMotorista, true);
        RouteLogger.gravar(os.getId(), resultado);
        return resultado;
    }

    /*
     * Aplica a ordem otimizada de volta nos {@link PontoColeta}s no banco de dados.
     *
     * <p>Renumera o campo {@code ordemParada} de cada ponto a partir de 1, refletindo
     * a sequência calculada pelo algoritmo. Erros individuais são logados mas não
     * interrompem o processamento dos demais pontos.
     *
     * @param resultado resultado retornado por {@link #otimizar} ou {@link #otimizarComGps}
     * @param pcRepo    repositório de pontos de coleta (responsabilidade do chamador)
     */
    public static void aplicarOrdemOtimizada(RouteResult resultado, PontoColetaRepository pcRepo) {
        if (resultado == null || resultado.getRotaOtimizada().isEmpty()) {
            LOG.warning("PathFindingUtil | Nenhuma rota otimizada para aplicar.");
            return;
        }

        List<Coordenada> rota = resultado.getRotaOtimizada();
        int aplicados = 0;

        for (int i = 0; i < rota.size(); i++) {
            Coordenada c = rota.get(i);
            if (c.getPontoColeta() != null) {
                PontoColeta pc = c.getPontoColeta();
                pc.setOrdemParada(i + 1);
                try {
                    pcRepo.atualizar(pc);
                    aplicados++;
                } catch (Exception e) {
                    LOG.severe("PathFindingUtil | Erro ao atualizar PontoColeta #" + pc.getId()
                            + ": " + e.getMessage());
                }
            }
        }

        LOG.info("PathFindingUtil | Ordem aplicada: " + aplicados + "/" + rota.size() + " pontos renumerados.");
    }

    // =========================================================================
    // Helpers privados — isolados neste util, sem vazar para outras camadas
    // =========================================================================

    /*
     * Coleta todos os PontosColeta de todos os Transfers da OS e os converte em Coordenadas.
     * Pontos sem coordenadas passam por geocodificação automática via Nominatim.
     */
    private static List<Coordenada> coletarCoordenadas(OrdemServico os) {
        List<Coordenada> coordenadas = new ArrayList<>();

        for (Transfer transfer : os.getTransfers()) {
            for (PontoColeta pc : transfer.getPontosColeta()) {
                Coordenada c = resolverCoordenada(pc);
                if (c != null) {
                    coordenadas.add(c);
                }
            }
        }

        return coordenadas;
    }

    /*
     * Converte um {@link PontoColeta} em {@link Coordenada}.
     *
     * <p>Se as coordenadas forem inválidas (null ou zeradas), tenta geocodificação
     * via Nominatim. Se falhar, retorna {@code null} e o ponto é ignorado com aviso.
     */
    private static Coordenada resolverCoordenada(PontoColeta pc) {
        boolean coordenadasValidas =
                pc.getLatitude()  != null && pc.getLongitude() != null
                        && (Math.abs(pc.getLatitude())  > 0.0001
                        ||  Math.abs(pc.getLongitude()) > 0.0001);

        if (coordenadasValidas) {
            return new Coordenada(pc.getLatitude(), pc.getLongitude(), pc.getLocalColeta(), pc);
        }

        LOG.info("PathFindingUtil | PontoColeta #" + pc.getId()
                + " (\"" + pc.getLocalColeta() + "\") sem coordenadas. Tentando geocodificação...");

        try {
            Coordenada geocodificada = GeocodingService.resolver(
                    pc.getLocalColeta() + ", Foz do Iguaçu, Brasil"
            );

            // Nominatim exige ≤ 1 req/s por IP — sleep obrigatório para evitar HTTP 429.
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            pc.setLatitude(geocodificada.getLatitude());
            pc.setLongitude(geocodificada.getLongitude());

            LOG.info("PathFindingUtil | Geocodificação OK: \"" + pc.getLocalColeta()
                    + "\" → " + geocodificada.getLatitude() + ", " + geocodificada.getLongitude());

            return new Coordenada(pc.getLatitude(), pc.getLongitude(), pc.getLocalColeta(), pc);

        } catch (GeocodingService.GeocodingException e) {
            LOG.warning("PathFindingUtil | Geocodificação falhou para \""
                    + pc.getLocalColeta() + "\": " + e.getMessage()
                    + ". Ponto ignorado na otimização.");
            return null;
        }
    }

    /*
     * Extrai a posição GPS do motorista como {@link Coordenada}.
     *
     * @param motorista motorista já carregado do banco (com posição atualizada)
     * @return coordenada do motorista, ou {@code null} se GPS não cadastrado ou zerado
     */
    private static Coordenada resolverPosicaoMotorista(Motorista motorista) {
        if (motorista == null) return null;

        Double lat = motorista.getLatitudeAtual();
        Double lon = motorista.getLongitudeAtual();

        if (lat == null || lon == null
                || (Math.abs(lat) < 0.0001 && Math.abs(lon) < 0.0001)) {
            return null;
        }

        return new Coordenada(lat, lon, "Posição atual de " + motorista.getNome());
    }

    /*
     * Cria um {@link RouteResult} vazio com mensagem de aviso logada.
     * Retorno limpo para entradas inválidas — nunca lança exceção para o chamador.
     */
    private static RouteResult resultadoVazio(String motivo) {
        LOG.warning("PathFindingUtil | " + motivo);
        return new RouteResult(
                List.of(),
                0.0,
                List.of("[AVISO] " + motivo),
                RouteResult.ModoCalculo.HAVERSINE
        );
    }
}
