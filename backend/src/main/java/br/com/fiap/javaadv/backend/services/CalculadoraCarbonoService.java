////package br.com.fiap.javaadv.backend.services;
////
////import br.com.fiap.javaadv.backend.integration.PlanetApiClient;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.locationtech.jts.geom.Geometry;
////import org.springframework.beans.factory.annotation.Value;
////import org.springframework.stereotype.Service;
////
////import java.util.Map;
////
////@Slf4j
////@Service
////@RequiredArgsConstructor
////public class CalculadoraCarbonoService {
////
////    private final PlanetApiClient planetApiClient;
////
////    @Value("${planet.mock.enabled:true}")
////    private boolean mockEnabled;
////
////    @Value("${calculadora.carbono.fator_conversao:0.47}")
////    private double fatorConversao;
////
////    @Value("${calculadora.carbono.biomassa_media_ton_ha:150.0}")
////    private double biomassaMediaTonHa;
////
////    @Value("${calculadora.carbono.valor_minimo:1.0}")
////    private double valorMinimo;
////
////    /**
////     * Calcula o estoque de carbono usando dados reais do Planet Insights
////     */
////    public Double calcularEstoque(Geometry geometry) {
////        if (geometry == null || geometry.isEmpty()) {
////            log.warn("Geometria nula ou vazia - usando valor mínimo");
////            return valorMinimo;
////        }
////
////        long startTime = System.currentTimeMillis();
////
////        try {
////            if (!mockEnabled) {
////                // Usar API real do Planet
////                log.info("🛰️ Calculando carbono com Planet Insights API (real)");
////                Map<String, Object> dadosPlanet = planetApiClient.calcularBiomassa(geometry);
////                Double carbonoTotal = (Double) dadosPlanet.get("carbonoTotalTon");
////
////                if (carbonoTotal != null && carbonoTotal > 0) {
////                    long duration = System.currentTimeMillis() - startTime;
////                    log.info("✅ Carbono calculado via Planet API: {} tCO2 (tempo: {} ms)", carbonoTotal, duration);
////                    return carbonoTotal;
////                }
////            }
////
////            // Fallback: cálculo baseado na área (simulação)
////            log.info("📐 Usando cálculo baseado em área (simulação)");
////            return calcularPorArea(geometry);
////
////        } catch (Exception e) {
////            log.error("❌ Erro no cálculo de carbono: {}", e.getMessage());
////            return calcularPorArea(geometry);
////        }
////    }
////
////    /**
////     * Calcula estoque de carbono com base no NDVI (mais preciso)
////     */
////    public Double calcularEstoquePorNDVI(Geometry geometry) {
////        if (geometry == null || geometry.isEmpty()) {
////            return valorMinimo;
////        }
////
////        try {
////            Double ndvi = planetApiClient.obterNDVI(geometry);
////            double areaHa = calcularAreaHectares(geometry);
////
////            // Fórmula baseada em NDVI: Biomassa = NDVI * 250 t/ha
////            double biomassaPorNDVI = ndvi * 250.0;
////            double carbonoPorNDVI = biomassaPorNDVI * fatorConversao;
////            double carbonoTotal = carbonoPorNDVI * areaHa;
////
////            log.info("🌿 Cálculo por NDVI: NDVI={}, Área={}ha, Carbono={}tCO2", ndvi, areaHa, carbonoTotal);
////            return Math.max(carbonoTotal, valorMinimo);
////
////        } catch (Exception e) {
////            log.error("Erro no cálculo por NDVI: {}", e.getMessage());
////            return calcularPorArea(geometry);
////        }
////    }
////
////    /**
////     * Cálculo de emergência baseado apenas na área
////     */
////    private Double calcularPorArea(Geometry geometry) {
////        double areaM2 = geometry.getArea();
////        double areaHectares = Math.abs(areaM2 / 10000.0);
////
////        double carbonoCalculado = areaHectares * biomassaMediaTonHa * fatorConversao;
////        double resultado = Math.max(carbonoCalculado, valorMinimo);
////
////        log.info("📏 Cálculo por área - {} hectares = {} tCO2",
////                String.format("%.2f", areaHectares),
////                String.format("%.2f", resultado));
////
////        return resultado;
////    }
////
////    /**
////     * Calcula área em hectares
////     */
////    private double calcularAreaHectares(Geometry geometry) {
////        double areaM2 = geometry.getArea();
////        return Math.abs(areaM2 / 10000.0);
////    }
////
////    /**
////     * Obtém apenas o NDVI da área
////     */
////    public Double obterNDVI(Geometry geometry) {
////        if (geometry == null) return 0.5;
////        return planetApiClient.obterNDVI(geometry);
////    }
////
////    /**
////     * Obtém classificação do solo
////     */
////    public String obterClassificacaoSolo(Geometry geometry) {
////        if (geometry == null) return "desconhecido";
////        return planetApiClient.obterClassificacaoSolo(geometry);
////    }
////
////    /**
////     * Obtém relatório completo de carbono
////     */
////    public Map<String, Object> obterRelatorioCompleto(Geometry geometry) {
////        Map<String, Object> relatorio = new java.util.HashMap<>();
////
////        double areaHa = calcularAreaHectares(geometry);
////        Double ndvi = obterNDVI(geometry);
////        String classificacao = obterClassificacaoSolo(geometry);
////        Double carbono = calcularEstoque(geometry);
////
////        relatorio.put("areaHectares", areaHa);
////        relatorio.put("ndvi", ndvi);
////        relatorio.put("classificacaoSolo", classificacao);
////        relatorio.put("carbonoTotalTon", carbono);
////        relatorio.put("dataCalculo", java.time.LocalDateTime.now().toString());
////        relatorio.put("metodo", mockEnabled ? "SIMULADO" : "PLANET_API");
////
////        return relatorio;
////    }
////}
//
//package br.com.fiap.javaadv.backend.services;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.geom.Polygon;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CalculadoraCarbonoService {
//
//    private static final double RAIO_TERRA_METROS = 6371000.0;
//
//    @Value("${calculadora.carbono.fator_conversao:0.47}")
//    private double fatorConversao;
//
//    @Value("${calculadora.carbono.biomassa_media_ton_ha:150.0}")
//    private double biomassaMediaTonHa;
//
//    @Value("${calculadora.carbono.valor_minimo:1.0}")
//    private double valorMinimo;
//
//    /**
//     * Calcula o estoque de carbono baseado na geometria (polígono)
//     * Fórmula: Carbono = Área (hectares) × Biomassa média (t/ha) × Fator de conversão
//     */
//    public Double calcularEstoque(Geometry geometry) {
//        if (geometry == null || geometry.isEmpty()) {
//            log.warn("⚠️ Geometria nula ou vazia - usando valor mínimo: {} tCO₂", valorMinimo);
//            return valorMinimo;
//        }
//
//        log.info("🌿 Iniciando cálculo de carbono para geometria...");
//
//        // Calcular área em hectares
//        double areaHectares = calcularAreaHectares(geometry);
//
//        // Validar área calculada
//        if (areaHectares <= 0) {
//            log.warn("⚠️ Área calculada é zero ou negativa: {} hectares", areaHectares);
//            return valorMinimo;
//        }
//
//        // Calcular carbono
//        double carbonoCalculado = areaHectares * biomassaMediaTonHa * fatorConversao;
//        double resultado = Math.max(carbonoCalculado, valorMinimo);
//
//        log.info("✅ Cálculo concluído:");
//        log.info("   📐 Área: {} hectares", String.format("%.2f", areaHectares));
//        log.info("   🌿 Biomassa média: {} t/ha", biomassaMediaTonHa);
//        log.info("   🔄 Fator de conversão: {}", fatorConversao);
//        log.info("   💚 Carbono total: {} tCO₂", String.format("%.2f", resultado));
//
//        return resultado;
//    }
//
//    /**
//     * Calcula a área em hectares a partir da geometria
//     * Suporta coordenadas geográficas (latitude/longitude)
//     */
//    public double calcularAreaHectares(Geometry geometry) {
//        if (geometry == null) return 0.0;
//
//        // Se for um polígono, calcular área esférica corretamente
//        if (geometry instanceof Polygon polygon) {
//            return calcularAreaHectaresPoligono(polygon);
//        }
//
//        // Fallback: área plana (menos precisa)
//        double areaM2 = geometry.getArea();
//        double areaHectares = Math.abs(areaM2 / 10000.0);
//
//        // Verificar se a área é plausível (se for muito pequena, pode ser graus)
//        if (areaHectares < 0.01 && areaM2 > 0 && areaM2 < 100) {
//            log.warn("⚠️ Área suspeita: {} m² ({} ha). Convertendo de graus para hectares...",
//                    String.format("%.2f", areaM2),
//                    String.format("%.6f", areaHectares));
//            // Estimativa: 1 grau quadrado ≈ 10.000 km² = 1.000.000 hectares
//            areaHectares = areaM2 * 1_000_000;
//            log.info("🔄 Área convertida: {} hectares", String.format("%.2f", areaHectares));
//        }
//
//        return areaHectares;
//    }
//
//    /**
//     * Calcula área de um polígono em hectares usando coordenadas esféricas
//     * (mais preciso para coordenadas geográficas)
//     */
//    private double calcularAreaHectaresPoligono(Polygon polygon) {
//        org.locationtech.jts.geom.Coordinate[] coords = polygon.getCoordinates();
//        double areaMetrosQuadrados = 0.0;
//
//        // Algoritmo da área esférica (fórmula do elipsóide)
//        for (int i = 0; i < coords.length - 1; i++) {
//            double lat1 = Math.toRadians(coords[i].y);
//            double lon1 = Math.toRadians(coords[i].x);
//            double lat2 = Math.toRadians(coords[i + 1].y);
//            double lon2 = Math.toRadians(coords[i + 1].x);
//
//            areaMetrosQuadrados += (lon2 - lon1) * Math.sin((lat2 + lat1) / 2);
//        }
//
//        areaMetrosQuadrados = Math.abs(areaMetrosQuadrados * RAIO_TERRA_METROS * RAIO_TERRA_METROS);
//        double areaHectares = areaMetrosQuadrados / 10000.0;
//
//        log.debug("🔍 Área esférica calculada: {} m² = {} hectares",
//                String.format("%.2f", areaMetrosQuadrados),
//                String.format("%.2f", areaHectares));
//
//        return areaHectares;
//    }
//}

package br.com.fiap.javaadv.backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculadoraCarbonoService {

    private static final double RAIO_TERRA_METROS = 6371000.0;

    @Value("${calculadora.carbono.fator_conversao:0.47}")
    private double fatorConversao;

    @Value("${calculadora.carbono.biomassa_media_ton_ha:150.0}")
    private double biomassaMediaTonHa;

    @Value("${calculadora.carbono.sequestro_mensal_ton_ha:0.5}")
    private double sequestroMensalTonHa;

    @Value("${calculadora.carbono.valor_minimo:1.0}")
    private double valorMinimo;

    /**
     * Calcula o estoque de carbono baseado na geometria (carbono total acumulado)
     * Fórmula: Carbono = Área (hectares) × Biomassa média (t/ha) × Fator de conversão
     */
    public Double calcularEstoqueTotal(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            log.warn("⚠️ Geometria nula ou vazia - usando valor mínimo: {} tCO₂", valorMinimo);
            return valorMinimo;
        }

        log.info("🌿 Iniciando cálculo de carbono TOTAL para geometria...");

        double areaHectares = calcularAreaHectares(geometry);

        if (areaHectares <= 0) {
            log.warn("⚠️ Área calculada é zero ou negativa: {} hectares", areaHectares);
            return valorMinimo;
        }

        double carbonoCalculado = areaHectares * biomassaMediaTonHa * fatorConversao;
        double resultado = Math.max(carbonoCalculado, valorMinimo);

        log.info("✅ Cálculo de carbono TOTAL concluído:");
        log.info("   📐 Área: {} hectares", String.format("%.2f", areaHectares));
        log.info("   💚 Carbono total acumulado: {} tCO₂", String.format("%.2f", resultado));

        return resultado;
    }

    /**
     * Calcula o estoque de carbono baseado no tempo de posse
     * Fórmula: Carbono = Área (ha) × Sequestro mensal (t/ha/mês) × Meses de posse
     */
    public Double calcularEstoquePorTempo(Geometry geometry, Integer anoAquisicao, Integer mesAquisicao) {
        if (geometry == null || geometry.isEmpty()) {
            log.warn("⚠️ Geometria nula ou vazia - usando valor mínimo: {} tCO₂", valorMinimo);
            return valorMinimo;
        }

        double areaHectares = calcularAreaHectares(geometry);

        if (areaHectares <= 0) {
            log.warn("⚠️ Área calculada é zero ou negativa: {} hectares", areaHectares);
            return valorMinimo;
        }

        long mesesPosse = calcularMesesPosse(anoAquisicao, mesAquisicao);

        if (mesesPosse <= 0) {
            log.warn("⚠️ Data de aquisição inválida - usando valor mínimo");
            return valorMinimo;
        }

        double carbonoCalculado = areaHectares * sequestroMensalTonHa * mesesPosse;
        double resultado = Math.max(carbonoCalculado, valorMinimo);

        log.info("✅ Cálculo de carbono por tempo de posse concluído:");
        log.info("   📐 Área: {} hectares", String.format("%.2f", areaHectares));
        log.info("   📅 Meses de posse: {} meses", mesesPosse);
        log.info("   🌿 Sequestro mensal: {} tCO₂/ha/mês", sequestroMensalTonHa);
        log.info("   💚 Carbono acumulado: {} tCO₂", String.format("%.2f", resultado));

        return resultado;
    }

    /**
     * Calcula a quantidade de meses entre ano/mês de aquisição e hoje
     */
    private long calcularMesesPosse(Integer anoAquisicao, Integer mesAquisicao) {
        if (anoAquisicao == null || mesAquisicao == null) {
            log.warn("⚠️ Ano ou mês de aquisição não informado");
            return 0;
        }

        try {
            YearMonth dataAquisicao = YearMonth.of(anoAquisicao, mesAquisicao);
            YearMonth hoje = YearMonth.now();

            if (dataAquisicao.isAfter(hoje)) {
                log.warn("⚠️ Data de aquisição ({}/{}) é futura", mesAquisicao, anoAquisicao);
                return 0;
            }

            return ChronoUnit.MONTHS.between(dataAquisicao, hoje);
        } catch (Exception e) {
            log.error("❌ Erro ao calcular meses de posse: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Calcula o sequestro mensal de carbono para uma área
     */
    public Double calcularSequestroMensal(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return valorMinimo;
        }

        double areaHectares = calcularAreaHectares(geometry);
        double carbonoMensal = areaHectares * sequestroMensalTonHa;
        double resultado = Math.max(carbonoMensal, valorMinimo);

        log.info("📊 Sequestro mensal: {} tCO₂/mês para {} hectares",
                String.format("%.2f", resultado),
                String.format("%.2f", areaHectares));

        return resultado;
    }

    /**
     * Calcula o sequestro anual de carbono para uma área
     */
    public Double calcularSequestroAnual(Geometry geometry) {
        Double mensal = calcularSequestroMensal(geometry);
        double anual = mensal * 12;
        log.info("📊 Sequestro anual: {} tCO₂/ano", String.format("%.2f", anual));
        return anual;
    }

    /**
     * Calcula a área em hectares a partir da geometria
     */
    public double calcularAreaHectares(Geometry geometry) {
        if (geometry == null) return 0.0;

        if (geometry instanceof Polygon polygon) {
            return calcularAreaHectaresPoligono(polygon);
        }

        double areaM2 = geometry.getArea();
        double areaHectares = Math.abs(areaM2 / 10000.0);

        if (areaHectares < 0.01 && areaM2 > 0 && areaM2 < 100) {
            log.warn("⚠️ Área suspeita: {} m² ({} ha). Convertendo de graus para hectares...",
                    String.format("%.2f", areaM2),
                    String.format("%.6f", areaHectares));
            areaHectares = areaM2 * 1_000_000;
            log.info("🔄 Área convertida: {} hectares", String.format("%.2f", areaHectares));
        }

        return areaHectares;
    }

    /**
     * Calcula área de um polígono em hectares usando coordenadas esféricas
     */
    private double calcularAreaHectaresPoligono(Polygon polygon) {
        org.locationtech.jts.geom.Coordinate[] coords = polygon.getCoordinates();
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

        log.debug("🔍 Área esférica calculada: {} m² = {} hectares",
                String.format("%.2f", areaMetrosQuadrados),
                String.format("%.2f", areaHectares));

        return areaHectares;
    }
}