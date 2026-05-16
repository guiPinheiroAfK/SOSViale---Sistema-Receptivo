package br.com.sosviale.service;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PontoColetaRepository;
import br.com.sosviale.service.pathfinding.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class PathFinding {

    private static final Logger LOG = Logger.getLogger(PathFinding.class.getName());

    private PathFinding() {}
    // API pública

    public static RouteResult otimizar(OrdemServico os) {
        if (os == null) {
            return resultadoVazio("OS não pode ser nula.");
        }

        List<Coordenada> pontos = coletarCoordenadas(os);

        if (pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto de coleta encontrado na OS #" + os.getId()
                    + ". Cadastre pontos de coleta nos transfers antes de otimizar.");
        }

        LOG.info("Iniciando otimização básica para OS #" + os.getId()
                + " com " + pontos.size() + " pontos.");

        RouteResult resultado = RouteOptimizer.otimizar(pontos, null, false);
        RouteLogger.gravar(os.getId(), resultado);
        return resultado;
    }


     /*MODO GPS + ESTRADA: otimiza usando a posição real do motorista como ponto
     de partida e distâncias reais de estrada via OSRM
     se o motorista não tiver GPS cadastrado, usa Haversine sem ponto de partida
     se o OSRM falhar, faz fallback automático para Haversine*/
    public static RouteResult otimizarComGps(OrdemServico os,
                                             MotoristaRepository motoristaRepo,
                                             PontoColetaRepository pcRepo) {
        if (os == null) {
            return resultadoVazio("OS não pode ser nula.");
        }

        List<Coordenada> pontos = coletarCoordenadas(os);

        if (pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto de coleta encontrado na OS #" + os.getId() + ".");
        }

        // Tenta obter posição GPS do motorista (buscamos do banco para garantir a posição mais recente)
        Coordenada posicaoMotorista = resolverPosicaoMotorista(os.getMotorista(), motoristaRepo);

        if (posicaoMotorista == null) {
            LOG.warning("Motorista da OS #" + os.getId()
                    + " não tem posição GPS cadastrada. Otimizando sem ponto de partida físico.");
        } else {
            LOG.info("Posição GPS do motorista encontrada: " + posicaoMotorista);
        }

        LOG.info("Iniciando otimização GPS+OSRM para OS #" + os.getId()
                + " com " + pontos.size() + " pontos.");

        RouteResult resultado = RouteOptimizer.otimizar(pontos, posicaoMotorista, true);
        RouteLogger.gravar(os.getId(), resultado);
        return resultado;
    }

    // splica a ordem otimizada de volta nos PontosColeta no banco de dados.

    public static void aplicarOrdemOtimizada(RouteResult resultado, PontoColetaRepository pcRepo) {
        if (resultado == null || resultado.getRotaOtimizada().isEmpty()) {
            LOG.warning("Nenhuma rota otimizada para aplicar.");
            return;
        }

        List<Coordenada> rota = resultado.getRotaOtimizada();
        for (int i = 0; i < rota.size(); i++) {
            Coordenada c = rota.get(i);
            if (c.getPontoColeta() != null) {
                PontoColeta pc = c.getPontoColeta();
                pc.setOrdemParada(i + 1); // renumera a partir de 1
                try {
                    pcRepo.atualizar(pc);
                } catch (Exception e) {
                    LOG.severe("Erro ao atualizar ordem do PontoColeta #" + pc.getId()
                            + ": " + e.getMessage());
                }
            }
        }

        LOG.info("Ordem otimizada aplicada: "
                + rota.size() + " pontos renumerados no banco.");
    }

    // Helpers privados
    // coleta todos os PontosColeta de todos os Transfers da OS e os converte em Coordenadas. pontos com coordenadas zeradas passam por geocodificação.

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

    //Converte um PontoColeta em Coordenada.

    private static Coordenada resolverCoordenada(PontoColeta pc) {
        boolean coordenadasValidas =
                pc.getLatitude()  != null && pc.getLongitude() != null
                        && (Math.abs(pc.getLatitude())  > 0.0001
                        || Math.abs(pc.getLongitude()) > 0.0001);

        if (coordenadasValidas) {
            return new Coordenada(pc.getLatitude(), pc.getLongitude(), pc.getLocalColeta(), pc);
        }

        // coordenadas zeradas — tenta geocodificação automática
        LOG.info("PontoColeta #" + pc.getId() + " (" + pc.getLocalColeta()
                + ") sem coordenadas. Tentando geocodificação via Nominatim...");

        try {
            Coordenada geocodificada = GeocodingService.resolver(
                    pc.getLocalColeta() + ", Foz do Iguaçu, Brasil"
            );

            // Nominatim exige no máximo 1 req/segundo por IP.
            // Sem este delay, uma OS com 5+ pontos sem coordenadas dispara
            // todas as requisições em milissegundos e recebe HTTP 429 (bloqueio de IP).
            try { Thread.sleep(1000); } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            // melhora o PontoColeta em memória (o chamador pode persistir depois)
            pc.setLatitude(geocodificada.getLatitude());
            pc.setLongitude(geocodificada.getLongitude());

            LOG.info("Geocodificação bem-sucedida: " + pc.getLocalColeta()
                    + " → " + geocodificada.getLatitude() + ", " + geocodificada.getLongitude());

            return new Coordenada(pc.getLatitude(), pc.getLongitude(), pc.getLocalColeta(), pc);

        } catch (GeocodingService.GeocodingException e) {
            LOG.warning("Geocodificação falhou para \"" + pc.getLocalColeta()
                    + "\": " + e.getMessage()
                    + ". Este ponto será ignorado na otimização.");
            return null;
        }
    }

    /*busca a posição GPS atualizada do motorista no banco
      retorna null se não tiver GPS ou se as coordenadas forem zerada*/
    private static Coordenada resolverPosicaoMotorista(Motorista motorista,
                                                       MotoristaRepository motoristaRepo) {
        if (motorista == null) return null;

        // rebusca do banco para garantir a posição mais recente
        Motorista atualizado = motoristaRepo.buscarPorId((int) motorista.getId().longValue());

        if (atualizado == null) return null;

        Double lat = atualizado.getLatitudeAtual();
        Double lon = atualizado.getLongitudeAtual();

        if (lat == null || lon == null
                || (Math.abs(lat) < 0.0001 && Math.abs(lon) < 0.0001)) {
            return null; // sem posição GPS cadastrada
        }

        return new Coordenada(lat, lon,
                "Posição atual de " + atualizado.getNome());
    }

      //cria um RouteResult vazio com uma mensagem de aviso no log.
      //usado para retornar de forma limpa quando a entrada é inválida.

    private static RouteResult resultadoVazio(String motivo) {
        LOG.warning("PathFinding: " + motivo);
        return new RouteResult(
                List.of(),
                0.0,
                List.of("[AVISO] " + motivo),
                RouteResult.ModoCalculo.HAVERSINE
        );
    }
}
