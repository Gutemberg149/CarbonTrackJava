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