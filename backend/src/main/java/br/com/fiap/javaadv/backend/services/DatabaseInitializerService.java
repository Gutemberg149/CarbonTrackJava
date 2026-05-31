//package br.com.fiap.javaadv.backend.services;
//
//import br.com.fiap.javaadv.backend.datasource.repositories.*;
//import br.com.fiap.javaadv.backend.domainmodel.entities.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.locationtech.jts.geom.Polygon;
//import org.locationtech.jts.io.WKTReader;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.time.LocalDateTime;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DatabaseInitializerService {
//
//    private final UserRepository userRepository;
//    private final PropriedadeRepository propriedadeRepository;
//    private final CreditoCarbonoRepository creditoRepository;
//    private final CalculadoraCarbonoService calculadoraService;
//
//    @Transactional
//    public void executarCarga() {
//        if (userRepository.findByEmail("farmer@carbontrack.com").isPresent()) {
//            log.info(">>> Dados já encontrados. Pulando carga.");
//            return;
//        }
//
//        try {
//            log.info(">>> Iniciando carga inicial...");
//
//            var user = userRepository.save(User.builder()
//                    .nome("Carbon Farmer")
//                    .email("farmer@carbontrack.com")
//                    .senha("123456")
//                    .build());
//
//            String wkt = "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, -46.6433 -23.5605, -46.6433 -23.5505, -46.6333 -23.5505))";
//            var geometry = new WKTReader().read(wkt);
//
//            var propriedade = propriedadeRepository.save(Propriedade.builder()
//                    .nome("Fazenda Mata Atlântica")
//                    .endereco("Estrada Rural, 100")
//                    .dono(user)
//                    .geometria(String.valueOf((Polygon) geometry))
//                    .build());
//
//            creditoRepository.save(CreditoCarbono.builder()
//                    .quantidade(calculadoraService.calcularEstoque(geometry))
//                    .dataEmissao(LocalDateTime.now())
//                    .propriedade(propriedade)
//                    .build());
//
//            log.info(">>> Banco populado com sucesso!");
//        } catch (Exception e) {
//            log.error(">>> Erro fatal na carga: ", e);
//            throw new RuntimeException("Falha na carga inicial", e);
//        }
//    }
//}

package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.*;
import br.com.fiap.javaadv.backend.domainmodel.entities.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseInitializerService {

    private final UserRepository userRepository;
    private final PropriedadeRepository propriedadeRepository;
    private final CreditoCarbonoRepository creditoRepository;
    private final CalculadoraCarbonoService calculadoraService;

    private static final int SRID = 4674; // SIRGAS 2000 - recomendado para o Brasil
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    @Transactional
    public void executarCarga() {
        if (userRepository.findByEmail("farmer@carbontrack.com").isPresent()) {
            log.info(">>> Dados já encontrados. Pulando carga.");
            return;
        }

        try {
            log.info(">>> Iniciando carga inicial...");

            // 1. Criar usuário
            User user = userRepository.save(User.builder()
                    .nome("Carbon Farmer")
                    .email("farmer@carbontrack.com")
                    .senha("123456")
                    .build());
            log.info("✅ Usuário criado: {}", user.getNome());

            // 2. Criar geometria (polígono)
            String wkt = "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, -46.6433 -23.5605, -46.6433 -23.5505, -46.6333 -23.5505))";
            Polygon geometria = converterWktParaPolygon(wkt);

            if (geometria == null) {
                throw new RuntimeException("Erro ao converter geometria WKT");
            }

            // 3. Calcular área e carbono
            double areaHectares = calcularAreaHectares(geometria);
            double carbonoEstimado = calculadoraService.calcularEstoque(geometria);
            log.info("📐 Área calculada: {} hectares", areaHectares);
            log.info("🌿 Carbono estimado: {} tCO₂", carbonoEstimado);

            // 4. Criar propriedade
            Propriedade propriedade = propriedadeRepository.save(Propriedade.builder()
                    .nome("Fazenda Mata Atlântica")
                    .endereco("Estrada Rural, 100")
                    .cidade("São Paulo")
                    .estado("SP")
                    .areaHectares(areaHectares)
                    .carbonoEstimado(carbonoEstimado)
                    .geometria(geometria)
                    .dono(user)
                    .build());
            log.info("✅ Propriedade criada: {} (ID: {})", propriedade.getNome(), propriedade.getId());

            // 5. Criar crédito de carbono
            CreditoCarbono credito = creditoRepository.save(CreditoCarbono.builder()
                    .quantidade(carbonoEstimado)
                    .dataEmissao(LocalDateTime.now())
                    .propriedade(propriedade)
                    .build());
            log.info("✅ Crédito de carbono criado: {} tCO₂", credito.getQuantidade());

            log.info(">>> Banco populado com sucesso!");

        } catch (Exception e) {
            log.error(">>> Erro fatal na carga: ", e);
            throw new RuntimeException("Falha na carga inicial", e);
        }
    }

    /**
     * Converte WKT (Well-Known Text) para Polygon JTS
     */
    private Polygon converterWktParaPolygon(String wkt) {
        try {
            WKTReader reader = new WKTReader(geometryFactory);
            var geometry = reader.read(wkt);
            if (geometry instanceof Polygon polygon) {
                polygon.setSRID(SRID);
                log.debug("Geometria convertida com sucesso: {}", polygon.toText());
                return polygon;
            }
            log.error("A geometria não é um polígono válido");
            return null;
        } catch (Exception e) {
            log.error("Erro ao converter WKT para Polygon: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Calcula área em hectares a partir do Polygon
     */
    private Double calcularAreaHectares(Polygon polygon) {
        if (polygon == null) return 0.0;

        // O JTS retorna a área em unidades do sistema de coordenadas
        // Para SRID 4326 (graus decimais), a área é em graus quadrados
        // Convertendo para hectares (aproximado)
        double areaEmGraus = polygon.getArea();

        // Fator de conversão aproximado (1 grau quadrado ≈ 10000 hectares no equador)
        double areaHectares = areaEmGraus * 10000;

        // Garantir valor positivo
        return Math.abs(areaHectares);
    }
}