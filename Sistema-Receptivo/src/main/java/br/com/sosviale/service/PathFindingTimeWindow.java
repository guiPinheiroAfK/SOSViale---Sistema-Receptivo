package br.com.sosviale.service;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PontoColetaRepository;
import br.com.sosviale.service.pathfinding.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Logger;
public final class PathFindingTimeWindow {

    private static final Logger LOG = Logger.getLogger(PathFindingTimeWindow.class.getName());

    // Velocidade média urbana em Foz do Iguaçu (km/h)
    private static final double VELOCIDADE_MEDIA_KMH = 40.0;

    // Tempo de parada por embarque/desembarque (minutos)
    private static final int TEMPO_PARADA_MINUTOS = 5;

    private PathFindingTimeWindow() {}

    // API pública
    //otimiza rota de uma OS respeitando horários dos transfers
    public static RouteResult otimizarComTimeWindow(OrdemServico os) {
        if (os == null) {
            return resultadoVazio("OS não pode ser nula.");
        }

        List<TimeWindowCoordenada> pontos = coletarCoordenadasComTimeWindow(os);

        if (pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto de coleta encontrado na OS #" + os.getId());
        }

        LOG.info("PathFindingTimeWindow | Modo básico (Haversine) | OS #" + os.getId()
                + " com " + pontos.size() + " pontos");

        // Função que calcula tempo de viagem em minutos
        BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Integer> tempoViagemFn =
                (o, d) -> calcularTempoViagem(o, d, false);

        RouteResult resultado = ConstraintAwareRouteOptimizer.otimizarComTimeWindows(
                pontos, null, false, tempoViagemFn);

        RouteLogger.gravar(os.getId(), resultado);
        return resultado;
    }

    //otimiza rota com GPS do motorista e distâncias reais (OSRM) respeitando time windows.

    public static RouteResult otimizarComTimeWindowEGps(
            OrdemServico os,
            MotoristaRepository motoristaRepo) {

        if (os == null) {
            return resultadoVazio("OS não pode ser nula.");
        }

        List<TimeWindowCoordenada> pontos = coletarCoordenadasComTimeWindow(os);

        if (pontos.isEmpty()) {
            return resultadoVazio("Nenhum ponto de coleta encontrado na OS #" + os.getId());
        }

        // resolve posição do motorista
        TimeWindowCoordenada posicaoMotorista = resolverPosicaoMotoristaTW(
                os.getMotorista(), motoristaRepo);

        if (posicaoMotorista == null) {
            LOG.warning("Motorista da OS #" + os.getId()
                    + " sem GPS. Otimizando sem ponto de partida físico (Haversine).");
        } else {
            LOG.info("Posição GPS do motorista: " + posicaoMotorista);
        }

        LOG.info("PathFindingTimeWindow | Modo GPS+OSRM+TimeWindow | OS #" + os.getId()
                + " com " + pontos.size() + " pontos");

        // função que calcula tempo via OSRM
        BiFunction<TimeWindowCoordenada, TimeWindowCoordenada, Integer> tempoViagemFn =
                (o, d) -> calcularTempoViagem(o, d, true);

        RouteResult resultado = ConstraintAwareRouteOptimizer.otimizarComTimeWindows(
                pontos, posicaoMotorista, true, tempoViagemFn);

        RouteLogger.gravar(os.getId(), resultado);
        return resultado;
    }

    // aplica a ordem otimizada de volta no banco de dados.

    public static void aplicarOrdemOtimizada(RouteResult resultado, PontoColetaRepository pcRepo) {
        if (resultado == null || resultado.getRotaOtimizada().isEmpty()) {
            LOG.warning("Nenhuma rota otimizada para aplicar.");
            return;
        }

        List<Coordenada> rota = resultado.getRotaOtimizada();
        for (int i = 0; i < rota.size(); i++) {
            Coordenada c = rota.get(i);
            if (c instanceof TimeWindowCoordenada twc && twc.getPontoColeta() != null) {
                PontoColeta pc = twc.getPontoColeta();
                pc.setOrdemParada(i + 1);
                try {
                    pcRepo.atualizar(pc);
                } catch (Exception e) {
                    LOG.severe("Erro ao atualizar PontoColeta #" + pc.getId()
                            + ": " + e.getMessage());
                }
            }
        }

        LOG.info("Ordem otimizada aplicada: " + rota.size() + " pontos renumerados.");
    }

    // Helpers privados
    // coleta coordenadas com time windows de uma OS.
    //ordena automaticamente por horário (prioridade absoluta).

    private static List<TimeWindowCoordenada> coletarCoordenadasComTimeWindow(OrdemServico os) {
        List<TimeWindowCoordenada> coordenadas = new ArrayList<>();

        for (Transfer transfer : os.getTransfers()) {
            for (PontoColeta pc : transfer.getPontosColeta()) {
                TimeWindowCoordenada c = resolverCoordenadasTW(pc, transfer);
                if (c != null) {
                    coordenadas.add(c);
                }
            }
        }

        // Ordena por horário (prioridade absoluta)
        coordenadas.sort(Comparator.comparing(TimeWindowCoordenada::getHorarioPrevisto,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return coordenadas;
    }

    //converte PontoColeta em TimeWindowCoordenada.

    private static TimeWindowCoordenada resolverCoordenadasTW(PontoColeta pc, Transfer transfer) {
        boolean coordenadasValidas =
                pc.getLatitude() != null && pc.getLongitude() != null
                        && (Math.abs(pc.getLatitude()) > 0.0001
                        || Math.abs(pc.getLongitude()) > 0.0001);

        if (!coordenadasValidas) {
            // tenta geocodificação
            try {
                Coordenada geocodificada = GeocodingService.resolver(
                        pc.getLocalColeta() + ", Foz do Iguaçu, Brasil");
                pc.setLatitude(geocodificada.getLatitude());
                pc.setLongitude(geocodificada.getLongitude());

                try { Thread.sleep(1000); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

            } catch (GeocodingService.GeocodingException e) {
                LOG.warning("Geocodificação falhou para " + pc.getLocalColeta());
                return null;
            }
        }

        // extrai horário do PontoColeta (pode vir de várias fontes)
        LocalTime horario = extrairHorario(pc);

        return new TimeWindowCoordenada(
                pc.getLatitude(), pc.getLongitude(),
                pc.getLocalColeta(), pc, transfer, horario, 15);
    }

    private static LocalTime extrairHorario(PontoColeta pc) {
        if (pc.getHorarioPrevisto() != null) {
            return pc.getHorarioPrevisto();
        }
        // Se tiver outro campo de hora, adicione aqui
        return null;
    }

    // resolve posição GPS do motorista como TimeWindowCoordenada.

    private static TimeWindowCoordenada resolverPosicaoMotoristaTW(
            Motorista motorista,
            MotoristaRepository motoristaRepo) {

        if (motorista == null) return null;

        Motorista atualizado = motoristaRepo.buscarPorId((int) motorista.getId().longValue());
        if (atualizado == null) return null;

        Double lat = atualizado.getLatitudeAtual();
        Double lon = atualizado.getLongitudeAtual();

        if (lat == null || lon == null
                || (Math.abs(lat) < 0.0001 && Math.abs(lon) < 0.0001)) {
            return null;
        }

        return new TimeWindowCoordenada(lat, lon, "Posição atual de " + atualizado.getNome());
    }

    /* calcula tempo de viagem em minutos entre dois pontos
     Fórmula:tempo = (distância_km / velocidade_kmh) * 60 + tempo_parada*/
    private static Integer calcularTempoViagem(
            TimeWindowCoordenada origem,
            TimeWindowCoordenada destino,
            boolean usarOsrm) {

        double distanciaKm;
        if (usarOsrm) {
            distanciaKm = DistanceCalculator.osrm(origem, destino);
        } else {
            distanciaKm = DistanceCalculator.haversine(origem, destino);
        }

        // Tempo de viagem em minutos + tempo de parada
        double tempoViagem = (distanciaKm / VELOCIDADE_MEDIA_KMH) * 60.0;
        return (int) Math.ceil(tempoViagem) + TEMPO_PARADA_MINUTOS;
    }

    private static RouteResult resultadoVazio(String motivo) {
        LOG.warning("PathFindingTimeWindow: " + motivo);
        return new RouteResult(
                List.of(),
                0.0,
                List.of("[AVISO] " + motivo),
                RouteResult.ModoCalculo.HAVERSINE
        );
    }
}
