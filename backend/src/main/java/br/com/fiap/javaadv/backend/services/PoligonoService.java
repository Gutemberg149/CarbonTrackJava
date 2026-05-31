package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.PoligonoRepository;
import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.Poligono;
import br.com.fiap.javaadv.backend.domainmodel.entities.Propriedade;
import br.com.fiap.javaadv.backend.resources.dtos.PoligonoRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.PoligonoResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoligonoService {

    private final PoligonoRepository poligonoRepository;
    private final PropriedadeRepository propriedadeRepository;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public PoligonoResponseDTO criar(PoligonoRequestDTO request) {
        Propriedade propriedade = propriedadeRepository.findById(request.getPropriedadeId())
                .orElseThrow(() -> new NoSuchElementException("Propriedade não encontrada"));

        // Converte GeoJSON para WKT
        String wkt = converterGeoJsonParaWKT(request.getGeometriaGeoJson());

        // Calcula área
        Double areaHectares = request.getAreaHectares() != null
                ? request.getAreaHectares()
                : calcularAreaHectares(wkt);

        Poligono poligono = Poligono.builder()
                .geometria(wkt)
                .areaHectares(areaHectares)
                .propriedade(propriedade)
                .build();

        return toResponseDTO(poligonoRepository.save(poligono));
    }

    @Transactional(readOnly = true)
    public List<PoligonoResponseDTO> listarTodos() {
        return poligonoRepository.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PoligonoResponseDTO buscarPorId(UUID id) {
        return poligonoRepository.findById(id).map(this::toResponseDTO)
                .orElseThrow(() -> new NoSuchElementException("Polígono não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<PoligonoResponseDTO> buscarPorPropriedade(UUID propriedadeId) {
        return poligonoRepository.findByPropriedadeId(propriedadeId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PoligonoResponseDTO atualizar(UUID id, PoligonoRequestDTO request) {
        Poligono poligono = poligonoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Polígono não encontrado"));

        if (request.getGeometriaGeoJson() != null) {
            String wkt = converterGeoJsonParaWKT(request.getGeometriaGeoJson());
            poligono.setGeometria(wkt);
            poligono.setAreaHectares(calcularAreaHectares(wkt));
        }

        if (request.getAreaHectares() != null) {
            poligono.setAreaHectares(request.getAreaHectares());
        }

        return toResponseDTO(poligonoRepository.save(poligono));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!poligonoRepository.existsById(id)) {
            throw new NoSuchElementException("Polígono não encontrado");
        }
        poligonoRepository.deleteById(id);
    }

    private String converterGeoJsonParaWKT(String geoJson) {
        try {
            Map<String, Object> map = objectMapper.readValue(geoJson, Map.class);
            List<List<List<Double>>> coordinates = (List<List<List<Double>>>) map.get("coordinates");

            Coordinate[] coords = coordinates.get(0).stream()
                    .map(p -> new Coordinate(p.get(0), p.get(1)))
                    .toArray(Coordinate[]::new);

            Polygon polygon = geometryFactory.createPolygon(coords);
            polygon.setSRID(4326);
            return polygon.toText();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar geometria GeoJSON", e);
        }
    }

    private Double calcularAreaHectares(String wkt) {
        try {
            Geometry geometry = new WKTReader(geometryFactory).read(wkt);
            return Math.abs(geometry.getArea() * 10000); // metros quadrados para hectares
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Map<String, Object> wktToGeoJson(String wkt) {
        if (wkt == null) return null;
        try {
            Geometry geometry = new WKTReader(geometryFactory).read(wkt);
            Map<String, Object> geoJson = new HashMap<>();
            geoJson.put("type", "Polygon");
            geoJson.put("coordinates", List.of(Arrays.stream(geometry.getCoordinates())
                    .map(c -> List.of(c.x, c.y))
                    .collect(Collectors.toList())));
            return geoJson;
        } catch (Exception e) {
            return null;
        }
    }

    private PoligonoResponseDTO toResponseDTO(Poligono p) {
        return PoligonoResponseDTO.builder()
                .id(p.getId())
                .propriedadeId(p.getPropriedade().getId())
                .propriedadeNome(p.getPropriedade().getNome())
                .geometriaGeoJson(wktToGeoJson(p.getGeometria()))
                .geometriaWkt(p.getGeometria())
                .areaHectares(p.getAreaHectares())
                .build();
    }
}