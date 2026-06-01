package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.CreditoCarbonoRepository;
import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
import br.com.fiap.javaadv.backend.datasource.repositories.UserRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.CreditoCarbono;
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

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropriedadeService {

    private final PropriedadeRepository propriedadeRepository;
    private final UserRepository userRepository;
    private final CreditoCarbonoRepository creditoRepository;
    private final CalculadoraCarbonoService calculadoraCarbonoService;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4674);

    private static final double RAIO_TERRA_METROS = 6371000.0;

    @Transactional
    public PropriedadeResponseDTO salvar(PropriedadeRequestDTO request) {
        log.info("========================================");
        log.info("📌 Salvando nova propriedade: {}", request.getNome());
        log.info("========================================");

        User dono = userRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + request.getUsuarioId()));

        // Validar geometria obrigatória
        if ((request.getGeometriaWkt() == null || request.getGeometriaWkt().isEmpty()) &&
                (request.getCoordenadasJson() == null || request.getCoordenadasJson().isEmpty())) {
            throw new RuntimeException("É obrigatório fornecer uma geometria (WKT ou GeoJSON)");
        }

        // Validar ano e mês de aquisição
        Integer anoAquisicao = request.getAnoAquisicao();
        Integer mesAquisicao = request.getMesAquisicao();

        if (anoAquisicao == null || mesAquisicao == null) {
            YearMonth hoje = YearMonth.now();
            anoAquisicao = hoje.getYear();
            mesAquisicao = hoje.getMonthValue();
            log.info("📅 Data de aquisição não informada, usando data atual: {}/{}", mesAquisicao, anoAquisicao);
        }

        String geometriaWkt = null;
        Double areaHectares = null;
        Double carbonoEstimado = null;

        // Processar geometria WKT
        if (request.getGeometriaWkt() != null && !request.getGeometriaWkt().isEmpty()) {
            geometriaWkt = request.getGeometriaWkt();
            log.info("📐 Processando geometria WKT...");

            Polygon polygon = converterWktParaPolygon(geometriaWkt);
            if (polygon != null) {
                areaHectares = calcularAreaHectaresCorreta(polygon);
                // Usar o método que considera tempo de posse
                carbonoEstimado = calculadoraCarbonoService.calcularEstoquePorTempo(polygon, anoAquisicao, mesAquisicao);
                log.info("✅ Geometria processada - Área: {} ha, Carbono acumulado: {} tCO₂",
                        formatarDuasCasas(areaHectares),
                        formatarDuasCasas(carbonoEstimado));
            }
        }
        // Processar geometria GeoJSON
        else if (request.getCoordenadasJson() != null && !request.getCoordenadasJson().isEmpty()) {
            geometriaWkt = request.getCoordenadasJson();
            log.info("📐 Processando geometria GeoJSON...");

            Polygon polygon = converterGeoJsonParaPolygon(geometriaWkt);
            if (polygon != null) {
                geometriaWkt = polygonToWkt(polygon);
                areaHectares = calcularAreaHectaresCorreta(polygon);
                // Usar o método que considera tempo de posse
                carbonoEstimado = calculadoraCarbonoService.calcularEstoquePorTempo(polygon, anoAquisicao, mesAquisicao);
                log.info("✅ Geometria processada - Área: {} ha, Carbono acumulado: {} tCO₂",
                        formatarDuasCasas(areaHectares),
                        formatarDuasCasas(carbonoEstimado));
            }
        }

        Propriedade propriedade = Propriedade.builder()
                .nome(request.getNome())
                .endereco(request.getEndereco())
                .cidade(request.getCidade())
                .estado(request.getEstado())
                .cep(request.getCep())
                .anoAquisicao(anoAquisicao)
                .mesAquisicao(mesAquisicao)
                .areaHectares(areaHectares)
                .carbonoEstimado(carbonoEstimado)
                .geometria(geometriaWkt)
                .dono(dono)
                .build();

        propriedade = propriedadeRepository.save(propriedade);
        log.info("✅ Propriedade salva com sucesso. ID: {}", propriedade.getId());

        // ============================================
        // 🎯 CRIAR CRÉDITO DE CARBONO AUTOMATICAMENTE
        // ============================================
        if (carbonoEstimado != null && carbonoEstimado > 0) {
            CreditoCarbono credito = CreditoCarbono.builder()
                    .quantidade(carbonoEstimado)
                    .dataEmissao(LocalDateTime.now())
                    .propriedade(propriedade)
                    .build();
            creditoRepository.save(credito);
            log.info("✅💰 Crédito de carbono criado AUTOMATICAMENTE: {} tCO₂ para propriedade: {}",
                    formatarDuasCasas(carbonoEstimado),
                    propriedade.getNome());
        }

        log.info("========================================");
        return toResponseDTO(propriedade);
    }

    @Transactional(readOnly = true)
    public List<PropriedadeResponseDTO> listarTodas() {
        log.info("📋 Listando todas as propriedades");
        return propriedadeRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropriedadeResponseDTO buscarPorId(UUID id) {
        log.info("🔍 Buscando propriedade por ID: {}", id);
        Propriedade propriedade = propriedadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada com ID: " + id));
        return toResponseDTO(propriedade);
    }

    @Transactional
    public PropriedadeResponseDTO atualizar(UUID id, PropriedadeRequestDTO request) {
        log.info("🔄 Atualizando propriedade ID: {}", id);

        Propriedade propriedade = propriedadeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada com ID: " + id));

        // Atualizar campos básicos
        propriedade.setNome(request.getNome());
        propriedade.setEndereco(request.getEndereco());
        propriedade.setCidade(request.getCidade());
        propriedade.setEstado(request.getEstado());
        propriedade.setCep(request.getCep());

        boolean geometriaModificada = false;
        Double novaArea = null;
        Double novoCarbono = null;

        // Atualizar ano/mês de aquisição se fornecidos
        if (request.getAnoAquisicao() != null) {
            propriedade.setAnoAquisicao(request.getAnoAquisicao());
        }
        if (request.getMesAquisicao() != null) {
            propriedade.setMesAquisicao(request.getMesAquisicao());
        }

        // Processar geometria WKT
        if (request.getGeometriaWkt() != null && !request.getGeometriaWkt().isEmpty()) {
            String novaGeometriaWkt = request.getGeometriaWkt();

            if (!novaGeometriaWkt.equals(propriedade.getGeometria())) {
                propriedade.setGeometria(novaGeometriaWkt);
                Polygon polygon = converterWktParaPolygon(novaGeometriaWkt);
                if (polygon != null) {
                    novaArea = calcularAreaHectaresCorreta(polygon);
                    novoCarbono = calculadoraCarbonoService.calcularEstoquePorTempo(
                            polygon,
                            propriedade.getAnoAquisicao(),
                            propriedade.getMesAquisicao());
                    geometriaModificada = true;
                    log.info("🔄 Geometria WKT atualizada - Nova área: {} ha, Novo carbono: {} tCO₂",
                            formatarDuasCasas(novaArea),
                            formatarDuasCasas(novoCarbono));
                }
            }
        }
        // Processar geometria GeoJSON
        else if (request.getCoordenadasJson() != null && !request.getCoordenadasJson().isEmpty()) {
            String novaGeometriaWkt = request.getCoordenadasJson();
            Polygon polygon = converterGeoJsonParaPolygon(novaGeometriaWkt);
            if (polygon != null) {
                String novaGeometria = polygonToWkt(polygon);
                if (!novaGeometria.equals(propriedade.getGeometria())) {
                    propriedade.setGeometria(novaGeometria);
                    novaArea = calcularAreaHectaresCorreta(polygon);
                    novoCarbono = calculadoraCarbonoService.calcularEstoquePorTempo(
                            polygon,
                            propriedade.getAnoAquisicao(),
                            propriedade.getMesAquisicao());
                    geometriaModificada = true;
                    log.info("🔄 Geometria GeoJSON atualizada - Nova área: {} ha, Novo carbono: {} tCO₂",
                            formatarDuasCasas(novaArea),
                            formatarDuasCasas(novoCarbono));
                }
            }
        }

        if (geometriaModificada) {
            propriedade.setAreaHectares(novaArea);
            propriedade.setCarbonoEstimado(novoCarbono);

            // 🎯 Criar NOVO crédito quando a geometria mudar
            if (novoCarbono != null && novoCarbono > 0) {
                CreditoCarbono novoCredito = CreditoCarbono.builder()
                        .quantidade(novoCarbono)
                        .dataEmissao(LocalDateTime.now())
                        .propriedade(propriedade)
                        .build();
                creditoRepository.save(novoCredito);
                log.info("✅💰 NOVO crédito criado devido à atualização da geometria: {} tCO₂",
                        formatarDuasCasas(novoCarbono));
            }
        }

        if (request.getUsuarioId() != null) {
            User dono = userRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + request.getUsuarioId()));
            propriedade.setDono(dono);
            log.info("👤 Dono atualizado: {}", dono.getNome());
        }

        propriedade = propriedadeRepository.save(propriedade);
        log.info("✅ Propriedade atualizada com sucesso. ID: {}", propriedade.getId());

        return toResponseDTO(propriedade);
    }

    @Transactional
    public void deletar(UUID id) {
        log.info("🗑️ Deletando propriedade ID: {}", id);

        if (!propriedadeRepository.existsById(id)) {
            throw new RuntimeException("Propriedade não encontrada com ID: " + id);
        }

        // Deletar créditos associados
        List<CreditoCarbono> creditos = creditoRepository.findByPropriedadeId(id);
        if (!creditos.isEmpty()) {
            creditoRepository.deleteAll(creditos);
            log.info("💰 Créditos associados deletados: {} registros", creditos.size());
        }

        propriedadeRepository.deleteById(id);
        log.info("✅ Propriedade deletada com sucesso. ID: {}", id);
    }

    @SuppressWarnings("unchecked")
    private Polygon converterGeoJsonParaPolygon(String geoJson) {
        if (geoJson == null || geoJson.isEmpty()) return null;
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
            polygon.setSRID(4674);
            return polygon;
        } catch (Exception e) {
            log.error("Erro ao converter GeoJSON para Polygon: {}", e.getMessage());
            return null;
        }
    }

    private Polygon converterWktParaPolygon(String wkt) {
        if (wkt == null || wkt.isEmpty()) return null;
        try {
            WKTReader reader = new WKTReader(geometryFactory);
            Geometry geometry = reader.read(wkt);
            if (geometry instanceof Polygon polygon) {
                polygon.setSRID(4674);
                return polygon;
            }
            log.warn("WKT não é um Polygon válido: {}", wkt);
            return null;
        } catch (Exception e) {
            log.error("Erro ao converter WKT para Polygon: {}", e.getMessage());
            return null;
        }
    }

    private String polygonToWkt(Polygon polygon) {
        if (polygon == null) return null;
        WKTWriter writer = new WKTWriter();
        return writer.write(polygon);
    }

    private Double calcularAreaHectaresCorreta(Polygon polygon) {
        if (polygon == null) return 0.0;

        Coordinate[] coords = polygon.getCoordinates();
        double areaMetrosQuadrados = 0.0;

        for (int i = 0; i < coords.length - 1; i++) {
            double lat1 = Math.toRadians(coords[i].y);
            double lon1 = Math.toRadians(coords[i].x);
            double lat2 = Math.toRadians(coords[i + 1].y);
            double lon2 = Math.toRadians(coords[i + 1].x);

            areaMetrosQuadrados += (lon2 - lon1) * Math.sin((lat2 + lat1) / 2);
        }

        areaMetrosQuadrados = Math.abs(areaMetrosQuadrados * RAIO_TERRA_METROS * RAIO_TERRA_METROS);
        double areaHectares = areaMetrosQuadrados / 10000.0;

        log.debug("🔍 Área calculada: {} m² = {} hectares",
                formatarDuasCasas(areaMetrosQuadrados),
                formatarDuasCasas(areaHectares));

        return areaHectares;
    }

    private PropriedadeResponseDTO toResponseDTO(Propriedade propriedade) {
        String geometriaWkt = propriedade.getGeometria();

        Map<String, Object> geometriaGeoJson = null;
        if (geometriaWkt != null && !geometriaWkt.isEmpty()) {
            Polygon polygon = converterWktParaPolygon(geometriaWkt);
            if (polygon != null) {
                geometriaGeoJson = polygonToGeoJson(polygon);
            }
        }

        // Formatar números com 2 casas decimais
        Double areaFormatada = formatarDuasCasas(propriedade.getAreaHectares());
        Double carbonoFormatado = formatarDuasCasas(propriedade.getCarbonoEstimado());

        return new PropriedadeResponseDTO(
                propriedade.getId(),
                propriedade.getNome(),
                propriedade.getEndereco(),
                propriedade.getCidade(),
                propriedade.getEstado(),
                propriedade.getCep(),
                propriedade.getAnoAquisicao(),
                propriedade.getMesAquisicao(),
                areaFormatada,
                carbonoFormatado,
                geometriaWkt,
                geometriaGeoJson,
                propriedade.getDono() != null ? propriedade.getDono().getId() : null,
                propriedade.getDono() != null ? propriedade.getDono().getNome() : null,
                propriedade.getDono() != null ? propriedade.getDono().getEmail() : null
        );
    }

    private Map<String, Object> polygonToGeoJson(Polygon polygon) {
        if (polygon == null) return null;

        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "Polygon");

        Coordinate[] coords = polygon.getCoordinates();
        List<List<Double>> pontos = new ArrayList<>();

        for (Coordinate coord : coords) {
            pontos.add(List.of(coord.x, coord.y));
        }

        geoJson.put("coordinates", List.of(pontos));
        return geoJson;
    }

    private Double formatarDuasCasas(Double valor) {
        if (valor == null) return null;
        return Math.round(valor * 100.0) / 100.0;
    }

    private String formatarDuasCasas(double valor) {
        return String.format("%.2f", valor);
    }
}