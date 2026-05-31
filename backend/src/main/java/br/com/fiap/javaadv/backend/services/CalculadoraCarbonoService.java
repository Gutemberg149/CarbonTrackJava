package br.com.fiap.javaadv.backend.services;

import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pelo cálculo de estoque de carbono.
 * Utiliza geometria espacial JTS para determinar a área e aplicar
 * fatores de conversão padrão do mercado de carbono.
 */
@Slf4j
@Service
public class CalculadoraCarbonoService {


    private static final double FATOR_CARBONO = 0.47;
    private static final double BIOMASSA_MEDIA_TON_HECTARE = 150.0;


    private static final double VALOR_MINIMO_CARBONO = 1.0;

    /**
     * Calcula o estoque de carbono com base na geometria da propriedade.
     *
     * @param geometry Objeto JTS representando o polígono da propriedade.
     * @return Quantidade de toneladas de carbono calculada (mínimo 1.0 tCO2)
     */
    public Double calcularEstoque(Geometry geometry) {
        // Caso 1: Geometria nula ou vazia
        if (geometry == null || geometry.isEmpty()) {
            log.warn("Geometria nula ou vazia - usando valor mínimo padrão: {} tCO2", VALOR_MINIMO_CARBONO);
            return VALOR_MINIMO_CARBONO;
        }

        try {
            // 1. O JTS retorna a área em unidades do sistema de referência (m2 para SRID 4326/projetado)
            double areaM2 = geometry.getArea();

            // Validação adicional para área muito pequena ou inválida
            if (areaM2 <= 0) {
                log.warn("Área calculada é {} m² - inválida, usando valor mínimo", areaM2);
                return VALOR_MINIMO_CARBONO;
            }

            // 2. Converter para Hectares (1 ha = 10.000 m2)
            double areaHectares = areaM2 / 10000.0;

            // 3. Cálculo final: Área * Densidade * Fator
            double carbonoCalculado = areaHectares * BIOMASSA_MEDIA_TON_HECTARE * FATOR_CARBONO;

            // 4. Garantir que o valor seja positivo e ter no mínimo o valor mínimo
            double resultado = Math.max(carbonoCalculado, VALOR_MINIMO_CARBONO);

            log.info("Cálculo concluído - Área: {} m² ({} hectares) - Carbono estimado: {} tCO2",
                    areaM2, areaHectares, resultado);

            return resultado;

        } catch (Exception e) {
            log.error("Erro ao calcular estoque de carbono: {}", e.getMessage(), e);
            log.warn("Usando valor mínimo devido a erro: {} tCO2", VALOR_MINIMO_CARBONO);
            return VALOR_MINIMO_CARBONO;
        }
    }

    /**
     * Sobrecarga do método para aceitar String WKT (conveniência)
     *
     * @param wktGeometria String no formato Well-Known Text
     * @return Quantidade de toneladas de carbono calculada
     */
    public Double calcularEstoque(String wktGeometria) {
        if (wktGeometria == null || wktGeometria.isBlank()) {
            log.warn("WKT nulo ou vazio - usando valor mínimo padrão: {} tCO2", VALOR_MINIMO_CARBONO);
            return VALOR_MINIMO_CARBONO;
        }

        try {
            org.locationtech.jts.io.WKTReader reader = new org.locationtech.jts.io.WKTReader();
            Geometry geometry = reader.read(wktGeometria);
            return calcularEstoque(geometry);
        } catch (Exception e) {
            log.error("Erro ao converter WKT para Geometry: {}", e.getMessage());
            return VALOR_MINIMO_CARBONO;
        }
    }
}