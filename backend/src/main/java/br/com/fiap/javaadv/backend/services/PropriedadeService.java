//package br.com.fiap.javaadv.backend.services;
//
//import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
//import br.com.fiap.javaadv.backend.datasource.repositories.UserRepository;
//import br.com.fiap.javaadv.backend.domainmodel.entities.Propriedade;
//import br.com.fiap.javaadv.backend.domainmodel.entities.User;
//import br.com.fiap.javaadv.backend.resources.dtos.PropriedadeRequestDTO;
//import br.com.fiap.javaadv.backend.resources.dtos.PropriedadeResponseDTO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.locationtech.jts.geom.Coordinate;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.geom.Polygon;
//import org.locationtech.jts.io.WKTReader;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class PropriedadeService {
//
//    private final PropriedadeRepository propriedadeRepository;
//    private final UserRepository userRepository;
//
//    @Transactional
//    public PropriedadeResponseDTO salvar(PropriedadeRequestDTO request) {
//        log.info("Salvando nova propriedade: {}", request.getNome());
//
//        User dono = userRepository.findById(request.getUsuarioId())
//                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + request.getUsuarioId()));
//
//        String geometriaWkt = null;
//        if (request.getCoordenadasJson() != null) {
//            geometriaWkt = converterGeoJsonParaWKT(request.getCoordenadasJson());
//        }
//
//        Propriedade propriedade = Propriedade.builder()
//                .nome(request.getNome())
//                .endereco(request.getEndereco())
//                .cidade(request.getCidade())
//                .estado(request.getEstado())
//                .cep(request.getCep())
//                .areaHectares(request.getAreaHectares())
//                .carbonoEstimado(request.getCarbonoEstimado())
//                .geometria(geometriaWkt)
//                .dono(dono)
//                .build();
//
//        propriedade = propriedadeRepository.save(propriedade);
//        log.info("Propriedade salva com sucesso. ID: {}", propriedade.getId());
//
//        return toResponseDTO(propriedade);
//    }
//
//    @Transactional(readOnly = true)
//    public List<PropriedadeResponseDTO> listarTodas() {
//        return propriedadeRepository.findAll().stream()
//                .map(this::toResponseDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public PropriedadeResponseDTO buscarPorId(UUID id) {
//        Propriedade propriedade = propriedadeRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada com ID: " + id));
//        return toResponseDTO(propriedade);
//    }
//
//    @Transactional
//    public PropriedadeResponseDTO atualizar(UUID id, PropriedadeRequestDTO request) {
//        Propriedade propriedade = propriedadeRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada com ID: " + id));
//
//        propriedade.setNome(request.getNome());
//        propriedade.setEndereco(request.getEndereco());
//        propriedade.setCidade(request.getCidade());
//        propriedade.setEstado(request.getEstado());
//        propriedade.setCep(request.getCep());
//        propriedade.setAreaHectares(request.getAreaHectares());
//        propriedade.setCarbonoEstimado(request.getCarbonoEstimado());
//
//        if (request.getCoordenadasJson() != null) {
//            propriedade.setGeometria(converterGeoJsonParaWKT(request.getCoordenadasJson()));
//        }
//
//        if (request.getUsuarioId() != null) {
//            User dono = userRepository.findById(request.getUsuarioId())
//                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + request.getUsuarioId()));
//            propriedade.setDono(dono);
//        }
//
//        return toResponseDTO(propriedadeRepository.save(propriedade));
//    }
//
//    @Transactional
//    public void deletar(UUID id) {
//        if (!propriedadeRepository.existsById(id)) {
//            throw new RuntimeException("Propriedade não encontrada com ID: " + id);
//        }
//        propriedadeRepository.deleteById(id);
//    }
//
//    @SuppressWarnings("unchecked")
//    private String converterGeoJsonParaWKT(String geoJson) {
//        try {
//            Map<String, Object> map = new com.fasterxml.jackson.databind.ObjectMapper()
//                    .readValue(geoJson, Map.class);
//
//            // Formato: {"type": "Polygon", "coordinates": [[[lng, lat], [lng, lat], ...]]}
//            List<List<List<Double>>> coordinates = (List<List<List<Double>>>) map.get("coordinates");
//            List<List<Double>> anilloExterior = coordinates.get(0);
//
//            Coordinate[] coords = new Coordinate[anilloExterior.size()];
//            for (int i = 0; i < anilloExterior.size(); i++) {
//                List<Double> ponto = anilloExterior.get(i);
//                coords[i] = new Coordinate(ponto.get(0), ponto.get(1));
//            }
//
//            Polygon polygon = new org.locationtech.jts.geom.GeometryFactory().createPolygon(coords);
//            return polygon.toText();
//        } catch (Exception e) {
//            log.error("Erro ao converter GeoJSON para WKT: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    private Map<String, Object> wktToGeoJson(String wkt) {
//        if (wkt == null) return null;
//        try {
//            Geometry geometry = new WKTReader().read(wkt);
//            Map<String, Object> geoJson = new HashMap<>();
//            geoJson.put("type", "Polygon");
//
//            if (geometry instanceof Polygon polygon) {
//                Coordinate[] coords = polygon.getCoordinates();
//                List<double[]> pontos = new ArrayList<>();
//
//                for (Coordinate coord : coords) {
//                    pontos.add(new double[]{coord.x, coord.y});
//                }
//
//                if (!pontos.isEmpty()) {
//                    double[] primeiro = pontos.get(0);
//                    pontos.add(new double[]{primeiro[0], primeiro[1]});
//                }
//
//                geoJson.put("coordinates", new Object[]{pontos});
//            }
//
//            return geoJson;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private PropriedadeResponseDTO toResponseDTO(Propriedade propriedade) {
//        String geometriaWkt = propriedade.getGeometria();
//        Map<String, Object> geometriaGeoJson = wktToGeoJson(geometriaWkt);
//
//        UUID donoId = propriedade.getDono() != null ? propriedade.getDono().getId() : null;
//        String donoNome = propriedade.getDono() != null ? propriedade.getDono().getNome() : null;
//        String donoEmail = propriedade.getDono() != null ? propriedade.getDono().getEmail() : null;
//
//        // ✅ Agora são 14 argumentos na ordem correta do DTO:
//        return new PropriedadeResponseDTO(
//                propriedade.getId(),
//                propriedade.getNome(),
//                propriedade.getEndereco(),
//                propriedade.getCidade(),
//                propriedade.getEstado(),
//                propriedade.getCep(),
//                propriedade.getAreaHectares(),
//                propriedade.getCarbonoEstimado(),
//                geometriaWkt,
//                geometriaWkt,
//                geometriaGeoJson,
//                donoId,
//                donoNome,
//                donoEmail
//        );
//    }
//}
package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
import br.com.fiap.javaadv.backend.datasource.repositories.UserRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.Propriedade;
import br.com.fiap.javaadv.backend.domainmodel.entities.User;
import br.com.fiap.javaadv.backend.resources.dtos.PropriedadeRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.PropriedadeResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropriedadeService {

    private final PropriedadeRepository propriedadeRepository;
    private final UserRepository userRepository;
    private final CalculadoraCarbonoService calculadoraCarbonoService;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), Propriedade.SRID);

    @Transactional
    public PropriedadeResponseDTO salvar(PropriedadeRequestDTO request) {
        log.info("Salvando nova propriedade: {}", request.getNome());

        User dono = userRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + request.getUsuarioId()));

        // Converter geometria
        Polygon geometria = null;
        Double areaHectares = request.getAreaHectares();
        Double carbonoEstimado = request.getCarbonoEstimado();

        if (request.getCoordenadasJson() != null && !request.getCoordenadasJson().isEmpty()) {
            geometria = converterGeoJsonParaPolygon(request.getCoordenadasJson());
            if (geometria != null) {
                areaHectares = calcularAreaHectares(geometria);
                carbonoEstimado = calculadoraCarbonoService.calcularEstoque(geometria);
            }
        } else if (request.getGeometriaWkt() != null && !request.getGeometriaWkt().isEmpty()) {
            geometria = converterWktParaPolygon(request.getGeometriaWkt());
            if (geometria != null) {
                areaHectares = calcularAreaHectares(geometria);
                carbonoEstimado = calculadoraCarbonoService.calcularEstoque(geometria);
            }
        }

        Propriedade propriedade = Propriedade.builder()
                .nome(request.getNome())
                .endereco(request.getEndereco())
                .cidade(request.getCidade())
                .estado(request.getEstado())
                .cep(request.getCep())
                .areaHectares(areaHectares)
                .carbonoEstimado(carbonoEstimado)
                .geometria(geometria)
                .dono(dono)
                .build();

        propriedade = propriedadeRepository.save(propriedade);
        log.info("Propriedade salva com sucesso. ID: {}, Área: {} ha, Carbono: {} tCO₂",
                propriedade.getId(), propriedade.getAreaHectares(), propriedade.getCarbonoEstimado());

        return toResponseDTO(propriedade);
    }

    @Transactional(readOnly = true)
    public List<PropriedadeResponseDTO> listarTodas() {
        return propriedadeRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropriedadeResponseDTO buscarPorId(UUID id) {
        Propriedade propriedade = propriedadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada com ID: " + id));
        return toResponseDTO(propriedade);
    }

    @Transactional
    public PropriedadeResponseDTO atualizar(UUID id, PropriedadeRequestDTO request) {
        log.info("Atualizando propriedade ID: {}", id);

        Propriedade propriedade = propriedadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada com ID: " + id));

        propriedade.setNome(request.getNome());
        propriedade.setEndereco(request.getEndereco());
        propriedade.setCidade(request.getCidade());
        propriedade.setEstado(request.getEstado());
        propriedade.setCep(request.getCep());

        // Atualizar geometria se fornecida
        if (request.getCoordenadasJson() != null && !request.getCoordenadasJson().isEmpty()) {
            Polygon geometria = converterGeoJsonParaPolygon(request.getCoordenadasJson());
            if (geometria != null) {
                propriedade.setGeometria(geometria);
                propriedade.setAreaHectares(calcularAreaHectares(geometria));
                propriedade.setCarbonoEstimado(calculadoraCarbonoService.calcularEstoque(geometria));
            }
        } else if (request.getGeometriaWkt() != null && !request.getGeometriaWkt().isEmpty()) {
            Polygon geometria = converterWktParaPolygon(request.getGeometriaWkt());
            if (geometria != null) {
                propriedade.setGeometria(geometria);
                propriedade.setAreaHectares(calcularAreaHectares(geometria));
                propriedade.setCarbonoEstimado(calculadoraCarbonoService.calcularEstoque(geometria));
            }
        } else {
            // Atualizar área e carbono manualmente se fornecidos
            if (request.getAreaHectares() != null) {
                propriedade.setAreaHectares(request.getAreaHectares());
            }
            if (request.getCarbonoEstimado() != null) {
                propriedade.setCarbonoEstimado(request.getCarbonoEstimado());
            }
        }

        if (request.getUsuarioId() != null) {
            User dono = userRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + request.getUsuarioId()));
            propriedade.setDono(dono);
        }

        propriedade = propriedadeRepository.save(propriedade);
        log.info("Propriedade atualizada com sucesso. ID: {}", propriedade.getId());

        return toResponseDTO(propriedade);
    }

    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando propriedade ID: {}", id);
        if (!propriedadeRepository.existsById(id)) {
            throw new RuntimeException("Propriedade não encontrada com ID: " + id);
        }
        propriedadeRepository.deleteById(id);
        log.info("Propriedade deletada com sucesso. ID: {}", id);
    }

    /**
     * Converte GeoJSON para Polygon JTS
     */
    @SuppressWarnings("unchecked")
    private Polygon converterGeoJsonParaPolygon(String geoJson) {
        try {
            Map<String, Object> map = objectMapper.readValue(geoJson, Map.class);
            List<List<List<Double>>> coordinates = (List<List<List<Double>>>) map.get("coordinates");
            List<List<Double>> anelExterior = coordinates.get(0);

            Coordinate[] coords = new Coordinate[anelExterior.size()];
            for (int i = 0; i < anelExterior.size(); i++) {
                List<Double> ponto = anelExterior.get(i);
                coords[i] = new Coordinate(ponto.get(0), ponto.get(1));
            }

            Polygon polygon = geometryFactory.createPolygon(coords);
            polygon.setSRID(Propriedade.SRID);
            return polygon;
        } catch (Exception e) {
            log.error("Erro ao converter GeoJSON para Polygon: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converte WKT para Polygon JTS
     */
    private Polygon converterWktParaPolygon(String wkt) {
        try {
            WKTReader reader = new WKTReader(geometryFactory);
            Geometry geometry = reader.read(wkt);
            if (geometry instanceof Polygon polygon) {
                polygon.setSRID(Propriedade.SRID);
                return polygon;
            }
            return null;
        } catch (Exception e) {
            log.error("Erro ao converter WKT para Polygon: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converte Polygon JTS para WKT
     */
    private String polygonToWkt(Polygon polygon) {
        if (polygon == null) return null;
        WKTWriter writer = new WKTWriter();
        return writer.write(polygon);
    }

    /**
     * Converte Polygon JTS para GeoJSON
     */
    private Map<String, Object> polygonToGeoJson(Polygon polygon) {
        if (polygon == null) return null;

        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "Polygon");

        Coordinate[] coords = polygon.getCoordinates();
        List<List<Double>> pontos = new ArrayList<>();

        for (Coordinate coord : coords) {
            pontos.add(List.of(coord.x, coord.y));
        }

        // Fechar o polígono (adicionar primeiro ponto ao final)
        if (!pontos.isEmpty()) {
            pontos.add(List.of(coords[0].x, coords[0].y));
        }

        geoJson.put("coordinates", List.of(pontos));
        return geoJson;
    }

    /**
     * Calcula área em hectares a partir do Polygon
     */
    private Double calcularAreaHectares(Polygon polygon) {
        if (polygon == null) return 0.0;
        double areaM2 = polygon.getArea();
        return Math.abs(areaM2 / 10000.0);
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private PropriedadeResponseDTO toResponseDTO(Propriedade propriedade) {
        String geometriaWkt = polygonToWkt(propriedade.getGeometria());
        Map<String, Object> geometriaGeoJson = polygonToGeoJson(propriedade.getGeometria());

        return new PropriedadeResponseDTO(
                propriedade.getId(),
                propriedade.getNome(),
                propriedade.getEndereco(),
                propriedade.getCidade(),
                propriedade.getEstado(),
                propriedade.getCep(),
                propriedade.getAreaHectares(),
                propriedade.getCarbonoEstimado(),
                geometriaWkt,        // coordenadasJson (compatibilidade)
                geometriaWkt,        // geometriaWkt
                geometriaGeoJson,    // geometriaGeoJson
                propriedade.getDono() != null ? propriedade.getDono().getId() : null,
                propriedade.getDono() != null ? propriedade.getDono().getNome() : null,
                propriedade.getDono() != null ? propriedade.getDono().getEmail() : null
        );
    }
}