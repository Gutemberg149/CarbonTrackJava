package br.com.fiap.javaadv.backend.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanetApiClient {

    private final RestTemplate restTemplate;

    @Value("${planet.api.url:https://api.planet.com/data/v1}")
    private String apiUrl;


    @Value("${planet.auth.api_key:PLAKbac00399a1094547badda8caae30f3e4}")
    private String apiKey;

    @Value("${planet.mock.enabled:true}")
    private boolean mockEnabled;

    public Map<String, Object> calcularBiomassa(Geometry geometria) {
        double areaHa = Math.abs(geometria.getArea() * 111320 * 111320 / 10000.0);
        return calcularBiomassa(geometria, areaHa);
    }

    public Map<String, Object> calcularBiomassa(Geometry geometria, double areaHa) {
        if (geometria == null) return criarErro("Geometria nula");

        // Se mock estiver ativo OU se a apiKey estiver vazia, usa o mock/fallback
        if (mockEnabled || apiKey.isEmpty()) {
            log.warn("⚠️ API Key ausente ou Mock habilitado. Usando modo de segurança.");
            return mockEnabled ? executarMock(areaHa) : executarFallback(areaHa);
        }

        return executarChamadaReal(geometria, areaHa)
                .orElseGet(() -> executarFallback(areaHa));
    }

    private Optional<Map<String, Object>> executarChamadaReal(Geometry geometria, double areaHa) {
        String endpoint = apiUrl + "/quick-search";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "api-key " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(montarFiltro(geometria), headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("features")) {
                List<?> features = (List<?>) body.get("features");
                if (features != null && !features.isEmpty()) {
                    Map<?, ?> firstFeature = (Map<?, ?>) features.get(0);
                    Map<?, ?> props = (Map<?, ?>) firstFeature.get("properties");

                    return Optional.of(Map.of(
                            "carbonoTotalTon", realizarCalculoFinal(areaHa, props),
                            "metodo", "REAL_SATELLITE_DATA",
                            "status", "SUCCESS"
                    ));
                }
            }
        } catch (Exception e) {
            log.error("❌ Erro na integração Planet API: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private double realizarCalculoFinal(double areaHa, Map<?, ?> props) {
        double visibilidade = (props.get("visible_percent") instanceof Number n) ? n.doubleValue() : 50.0;
        return areaHa * (Math.max(visibilidade, 10.0) / 100.0) * 70.5;
    }

    private Map<String, Object> montarFiltro(Geometry geometria) {
        return Map.of(
                "item_types", List.of("PSScene"),
                "filter", Map.of("type", "AndFilter", "config", List.of(
                        Map.of("type", "GeometryFilter", "field_name", "geometry", "config",
                                Map.of("type", "Polygon", "coordinates", List.of(converterCoordenadas(geometria)))),
                        Map.of("type", "DateRangeFilter", "field_name", "acquired", "config",
                                Map.of("gte", LocalDate.now().minusMonths(6).toString() + "T00:00:00Z"))
                ))
        );
    }

    private List<List<Double>> converterCoordenadas(Geometry geometria) {
        return Arrays.stream(geometria.getCoordinates())
                .map(c -> List.of(c.x, c.y))
                .toList();
    }

    private Map<String, Object> executarFallback(double areaHa) {
        return Map.of("carbonoTotalTon", areaHa * 62.15, "metodo", "FALLBACK_CALCULATION", "status", "FALLBACK");
    }

    private Map<String, Object> executarMock(double areaHa) {
        return Map.of("carbonoTotalTon", areaHa * 70.5, "metodo", "MOCK", "status", "MOCK");
    }

    private Map<String, Object> criarErro(String msg) {
        return Map.of("erro", true, "mensagem", msg, "carbonoTotalTon", 0.0);
    }
}