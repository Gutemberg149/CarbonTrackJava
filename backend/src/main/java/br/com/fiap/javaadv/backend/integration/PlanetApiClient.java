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

    @Value("${planet.auth.api_key}")
    private String apiKey;

    @Value("${planet.mock.enabled:false}")
    private boolean mockEnabled;

    // --- SOBRECARGA 1: Compatibilidade (resolvendo o erro dos outros arquivos) ---
    public Map<String, Object> calcularBiomassa(Geometry geometria) {
        // Cálculo básico para evitar que o código quebre onde não passamos a área
        double areaHa = Math.abs(geometria.getArea() * 111320 * 111320 / 10000.0);
        return calcularBiomassa(geometria, areaHa);
    }


    public Map<String, Object> calcularBiomassa(Geometry geometria, double areaHa) {
        if (geometria == null) return criarErro("Geometria nula");
        if (mockEnabled) return executarMock(areaHa);

        return executarChamadaReal(geometria, areaHa)
                .orElseGet(() -> {
                    log.warn("🔄 Falha na API real. Aplicando fallback.");
                    return executarFallback(areaHa);
                });
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> executarChamadaReal(Geometry geometria, double areaHa) {
        String endpoint = apiUrl + "/quick-search";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "api-key " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(montarFiltro(geometria), headers);

        try {
            log.info("📡 Iniciando chamada REAL para Planet API...");
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, request, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("features")) {
                List<Map<String, Object>> features = (List<Map<String, Object>>) body.get("features");

                if (features != null && !features.isEmpty()) {
                    Map<String, Object> properties = (Map<String, Object>) features.get(0).get("properties");

                    return Optional.of(Map.of(
                            "carbonoTotalTon", realizarCalculoFinal(areaHa, properties),
                            "metodo", "REAL_SATELLITE_DATA",
                            "dataImagem", properties.getOrDefault("acquired", LocalDate.now().toString()),
                            "fonte", properties.getOrDefault("provider", "planetscope"),
                            "status", "SUCCESS"
                    ));
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("❌ Erro na integração Planet API: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private double realizarCalculoFinal(double areaHa, Map<String, Object> props) {
        Object visObj = props.getOrDefault("visible_percent", 50.0);
        double visibilidade = (visObj instanceof Number) ? ((Number) visObj).doubleValue() : 50.0;

        double visibilidadeAjustada = (visibilidade < 10.0) ? 50.0 : visibilidade;

        double fatorCarbono = 70.5;
        double resultado = areaHa * (visibilidadeAjustada / 100.0) * fatorCarbono;

        log.info("📊 Cálculo: {} ha | Visibilidade: {}% | Resultado: {} tCO2",
                String.format("%.2f", areaHa), visibilidadeAjustada, String.format("%.2f", resultado));

        return resultado;
    }

    private Map<String, Object> montarFiltro(Geometry geometria) {
        return Map.of(
                "item_types", List.of("PSScene"),
                "filter", Map.of(
                        "type", "AndFilter",
                        "config", List.of(
                                Map.of("type", "GeometryFilter", "field_name", "geometry",
                                        "config", Map.of("type", "Polygon", "coordinates", List.of(converterCoordenadas(geometria)))),
                                Map.of("type", "DateRangeFilter", "field_name", "acquired",
                                        "config", Map.of("gte", LocalDate.now().minusMonths(6).toString() + "T00:00:00Z"))
                        )
                )
        );
    }

    private List<List<Double>> converterCoordenadas(Geometry geometria) {
        return Arrays.stream(geometria.getCoordinates())
                .map(c -> List.of(c.x, c.y))
                .toList();
    }

    private Map<String, Object> executarFallback(double areaHa) {
        return Map.of(
                "carbonoTotalTon", areaHa * 62.15,
                "metodo", "FALLBACK_CALCULATION",
                "status", "FALLBACK"
        );
    }

    private Map<String, Object> executarMock(double areaHa) {
        return Map.of("carbonoTotalTon", areaHa * 70.5, "metodo", "MOCK", "status", "MOCK");
    }

    private Map<String, Object> criarErro(String msg) {
        return Map.of("erro", true, "mensagem", msg, "carbonoTotalTon", 0.0);
    }
}