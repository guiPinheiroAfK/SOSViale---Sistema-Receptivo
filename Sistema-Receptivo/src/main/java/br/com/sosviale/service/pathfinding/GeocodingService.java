package br.com.sosviale.service.pathfinding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.logging.Logger;

/*
 * Resolve endereços textuais para coordenadas geográficas (geocodificação direta).
 *
 * Utiliza a API pública Nominatim do OpenStreetMap, que é gratuita e não requer
 * chave de API. Termos de uso: máximo 1 requisição/segundo, User-Agent obrigatório.
 *
 * Contexto do sistema: região de Foz do Iguaçu/Tríplice Fronteira.
 * O bias de área é configurado para essa região, melhorando a precisão dos resultados.
 *
 * Quando a geocodificação falha (endereço ambíguo, sem rede), o sistema lança
 * {@link GeocodingException} com uma mensagem explicativa — o chamador decide se
 * cancela a operação ou pede ao usuário para inserir as coordenadas manualmente.
 */
public final class GeocodingService {

    private static final Logger LOG = Logger.getLogger(GeocodingService.class.getName());

    /* User-Agent obrigatório pela política de uso da Nominatim. */
    private static final String USER_AGENT = "SosVialeTransfer/1.0 (contato@sosviale.com.br)";

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1&countrycodes=br,py,ar";

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(6);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private GeocodingService() {}

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /*
     * Converte um endereço textual para coordenadas geográficas.
     *
     * @param endereco endereço legível (ex: "Aeroporto Internacional de Foz do Iguaçu")
     * @return coordenada com lat/lng resolvidos e o endereço como nome
     * @throws GeocodingException se o endereço não for encontrado ou a API falhar
     */
    public static Coordenada resolver(String endereco) throws GeocodingException {
        if (endereco == null || endereco.isBlank()) {
            throw new GeocodingException("Endereço não pode ser vazio.");
        }

        String encodedQuery = URLEncoder.encode(endereco, StandardCharsets.UTF_8);
        String url = String.format(NOMINATIM_URL, encodedQuery);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(HTTP_TIMEOUT)
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new GeocodingException("Nominatim retornou status " + response.statusCode()
                        + " para o endereço: " + endereco);
            }

            JsonNode resultados = MAPPER.readTree(response.body());

            if (!resultados.isArray() || resultados.size() == 0) {
                throw new GeocodingException("Endereço não encontrado: \"" + endereco
                        + "\". Tente um nome mais específico ou insira as coordenadas manualmente.");
            }

            JsonNode primeiro = resultados.get(0);
            double lat = primeiro.path("lat").asDouble();
            double lon = primeiro.path("lon").asDouble();

            LOG.info("Geocodificação OK: \"" + endereco + "\" → " + lat + ", " + lon);
            return new Coordenada(lat, lon, endereco);

        } catch (GeocodingException e) {
            throw e; // repropaga sem embrulhar
        } catch (Exception e) {
            throw new GeocodingException("Falha ao contatar Nominatim para \"" + endereco
                    + "\": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Exceção específica do serviço
    // -------------------------------------------------------------------------

    /*
     * Lançada quando não é possível resolver um endereço para coordenadas.
     * O chamador deve tratar esta exceção e oferecer entrada manual ao usuário.
     */
    public static class GeocodingException extends Exception {
        public GeocodingException(String message) {
            super(message);
        }
    }
}
