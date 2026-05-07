package br.com.sosviale.util.pathfinding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Calcula a distância entre dois pontos geográficos.
 *
 * Dois motores disponíveis:
 *
 *   HAVERSINE — cálculo matemático da distância em linha reta entre dois pontos
 *               na superfície esférica da Terra. Sem dependências externas.
 *               Suficiente para ordenar rotas urbanas curtas (margem ~5%).
 *
 *   OSRM      — Open Source Routing Machine (https://router.project-osrm.org).
 *               API pública e gratuita que retorna a distância real de estrada,
 *               respeitando o traçado viário. Requer conexão com a internet.
 *               Usado quando o admin ativa o modo GPS/localização real.
 *               Em caso de timeout ou falha de rede, faz fallback para Haversine.
 */
public final class DistanceCalculator {

    private static final Logger LOG = Logger.getLogger(DistanceCalculator.class.getName());

    /** Raio médio da Terra em km (WGS-84). */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /** Timeout de 4 segundos para chamadas OSRM — evita travar o algoritmo. */
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(4);

    /** URL base da API pública OSRM. Pode ser substituída por instância self-hosted. */
    private static final String OSRM_BASE_URL =
            "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DistanceCalculator() {}

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /**
     * Calcula a distância em km usando Haversine (linha reta).
     * Sempre disponível, sem requisitos de rede.
     */
    public static double haversine(Coordenada origem, Coordenada destino) {
        double latRad1 = Math.toRadians(origem.getLatitude());
        double latRad2 = Math.toRadians(destino.getLatitude());
        double dLat    = Math.toRadians(destino.getLatitude()  - origem.getLatitude());
        double dLon    = Math.toRadians(destino.getLongitude() - origem.getLongitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * Calcula a distância real de estrada em km via API OSRM.
     *
     * Se a API falhar (sem rede, timeout, resposta inválida), registra o erro
     * e retorna o valor Haversine como fallback — o algoritmo nunca para.
     *
     * @param origem    ponto de partida
     * @param destino   ponto de chegada
     * @return distância real em km, ou Haversine em caso de falha
     */
    public static double osrm(Coordenada origem, Coordenada destino) {
        // Locale.US obrigatório: em pt-BR, String.format usa vírgula como separador
        // decimal (ex: -54,49), o que causa HTTP 400 na API do OSRM.
        String url = String.format(
                java.util.Locale.US,
                OSRM_BASE_URL,
                origem.getLongitude(),  // OSRM usa ordem lon,lat
                origem.getLatitude(),
                destino.getLongitude(),
                destino.getLatitude()
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(HTTP_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = MAPPER.readTree(response.body());
                JsonNode routes = root.path("routes");
                if (routes.isArray() && routes.size() > 0) {
                    // OSRM retorna distância em metros
                    double metros = routes.get(0).path("distance").asDouble();
                    return metros / 1000.0;
                }
            }

            LOG.warning("OSRM retornou status " + response.statusCode()
                    + " para " + origem.getNome() + " → " + destino.getNome()
                    + ". Usando Haversine como fallback.");

        } catch (Exception e) {
            LOG.warning("OSRM indisponível (" + e.getMessage()
                    + "). Usando Haversine como fallback para "
                    + origem.getNome() + " → " + destino.getNome());
        }

        return haversine(origem, destino);
    }
}
