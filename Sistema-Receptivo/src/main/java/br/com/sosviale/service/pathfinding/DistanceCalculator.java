package br.com.sosviale.service.pathfinding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

// calcula a distância entre dois pontos geográficos.

public final class DistanceCalculator {

    private static final Logger LOG = Logger.getLogger(DistanceCalculator.class.getName());

    // raio médio da Terra em km
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Timeout de 4 segundos para chamadas OSRM — evita travar o algoritmo.
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(4);

    // URL base da API pública OSRM
    private static final String OSRM_BASE_URL =
            "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DistanceCalculator() {}

     // calcula a distância em km usando Haversine (linha reta).
     // sempre disponível, sem requisitos de rede.
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

    // calcula a distância real de estrada em km via API OSRM.
    public static double osrm(Coordenada origem, Coordenada destino) {
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
