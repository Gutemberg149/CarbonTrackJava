//package br.com.fiap.javaadv.backend.infrastructure;
//
//import br.com.fiap.javaadv.backend.datasource.repositories.*;
//import br.com.fiap.javaadv.backend.domainmodel.entities.*;
//import br.com.fiap.javaadv.backend.services.CalculadoraCarbonoService;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.io.WKTReader;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.LocalDateTime;
//
//@Slf4j
//@Configuration
//@RequiredArgsConstructor
//public class DataLoader {
//
//    private final UserRepository userRepository;
//    private final PropriedadeRepository propriedadeRepository;
//    private final CreditoCarbonoRepository creditoRepository;
//    private final CalculadoraCarbonoService calculadoraService;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Bean
//    public CommandLineRunner initDatabase() {
//        return args -> {
//            criarTabelaPropriedades();
//            criarTabelaPoligonos();
//            inserirDadosSeed();
//        };
//    }
//
//    private void criarTabelaPropriedades() {
//        try {
//            var result = entityManager.createNativeQuery(
//                    "SELECT table_name FROM user_tables WHERE table_name = 'TB_PROPRIEDADE_V2'"
//            ).getResultList();
//
//            if (result.isEmpty()) {
//
//                try {
//                    entityManager.createNativeQuery("DROP TABLE tb_propriedade").executeUpdate();
//                } catch (Exception e) { /* ignora */ }
//
//                entityManager.createNativeQuery(
//                        "CREATE TABLE tb_propriedade (" +
//                                "  id RAW(16) NOT NULL," +
//                                "  nome VARCHAR2(255)," +
//                                "  endereco VARCHAR2(500)," +
//                                "  cidade VARCHAR2(100)," +
//                                "  estado VARCHAR2(2)," +
//                                "  cep VARCHAR2(20)," +
//                                "  area_hectares NUMBER(19,2)," +
//                                "  carbono_estimado NUMBER(19,2)," +
//                                "  geometria CLOB," +
//                                "  dono_id RAW(16)," +
//                                "  CONSTRAINT pk_propriedade PRIMARY KEY (id)" +
//                                ")"
//                ).executeUpdate();
//                log.info(">>> Tabela tb_propriedade criada com CLOB!");
//            } else {
//                log.info(">>> Tabela tb_propriedade já existe.");
//            }
//        } catch (Exception e) {
//            log.error("Erro ao criar tabela tb_propriedade: {}", e.getMessage());
//        }
//    }
//
//    private void criarTabelaPoligonos() {
//        try {
//            var result = entityManager.createNativeQuery(
//                    "SELECT table_name FROM user_tables WHERE table_name = 'TB_POLIGONOS_V2'"
//            ).getResultList();
//
//            if (result.isEmpty()) {
//                try {
//                    entityManager.createNativeQuery("DROP TABLE tb_poligonos").executeUpdate();
//                } catch (Exception e) { /* ignora */ }
//
//                entityManager.createNativeQuery(
//                        "CREATE TABLE tb_poligonos_v2 (" +
//                                "  id RAW(16) NOT NULL," +
//                                "  geometria CLOB," +
//                                "  area_hectares NUMBER(19,2)," +
//                                "  propriedade_id RAW(16) NOT NULL," +
//                                "  CONSTRAINT pk_poligonos_v2 PRIMARY KEY (id)" +
//                                ")"
//                ).executeUpdate();
//                log.info(">>> Tabela tb_poligonos_v2 criada!");
//            } else {
//                log.info(">>> Tabela tb_poligonos_v2 já existe.");
//            }
//        } catch (Exception e) {
//            log.error("Erro ao criar tabela tb_poligonos: {}", e.getMessage());
//        }
//    }
//
//    private void inserirDadosSeed() {
//        if (userRepository.findByEmail("farmer@carbontrack.com").isPresent()) {
//            log.info(">>> Dados de seed já existem.");
//            return;
//        }
//
//        log.info(">>> Inserindo dados de seed...");
//
//        var user = userRepository.save(User.builder()
//                .nome("Carbon Farmer")
//                .email("farmer@carbontrack.com")
//                .senha("123456")
//                .build());
//
//        String wkt = "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, -46.6433 -23.5605, -46.6433 -23.5505, -46.6333 -23.5505))";
//
//        var propriedade = propriedadeRepository.save(Propriedade.builder()
//                .nome("Fazenda Mata Atlântica")
//                .endereco("Estrada Rural, 100")
//                .geometria(wkt)
//                .dono(user)
//                .build());
//
//        Double quantidade = calcularEstoque(wkt);
//
//        creditoRepository.save(CreditoCarbono.builder()
//                .quantidade(quantidade)
//                .dataEmissao(LocalDateTime.now())
//                .propriedade(propriedade)
//                .build());
//
//        log.info(">>> Seed inserido com sucesso!");
//    }
//
//    private Double calcularEstoque(String wkt) {
//        try {
//            Geometry geometry = new WKTReader().read(wkt);
//            return calculadoraService.calcularEstoque(geometry);
//        } catch (Exception e) {
//            log.error("Erro ao converter WKT: {}", e.getMessage());
//            return 0.0;
//        }
//    }
//}

package br.com.fiap.javaadv.backend.infrastructure;

import br.com.fiap.javaadv.backend.datasource.repositories.*;
import br.com.fiap.javaadv.backend.domainmodel.entities.*;
import br.com.fiap.javaadv.backend.services.CalculadoraCarbonoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataLoader {

    private final UserRepository userRepository;
    private final PropriedadeRepository propriedadeRepository;
    private final CreditoCarbonoRepository creditoRepository;
    private final CalculadoraCarbonoService calculadoraService;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int SRID = 4674;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Apenas para H2/DEV, não criar tabelas manualmente se usar Hibernate DDL
            // criarTabelaPropriedades();
            // criarTabelaPoligonos();
            inserirDadosSeed();
        };
    }

    private void criarTabelaPropriedades() {
        try {
            var result = entityManager.createNativeQuery(
                    "SELECT table_name FROM user_tables WHERE table_name = 'TB_PROPRIEDADE'"
            ).getResultList();

            if (result.isEmpty()) {
                entityManager.createNativeQuery(
                        "CREATE TABLE TB_PROPRIEDADE (" +
                                "  id RAW(16) NOT NULL," +
                                "  nome VARCHAR2(255)," +
                                "  endereco VARCHAR2(500)," +
                                "  cidade VARCHAR2(100)," +
                                "  estado VARCHAR2(2)," +
                                "  cep VARCHAR2(20)," +
                                "  area_hectares NUMBER(19,2)," +
                                "  carbono_estimado NUMBER(19,2)," +
                                "  geometria SDO_GEOMETRY," +
                                "  dono_id RAW(16)," +
                                "  CONSTRAINT pk_propriedade PRIMARY KEY (id)" +
                                ")"
                ).executeUpdate();
                log.info(">>> Tabela TB_PROPRIEDADE criada com SDO_GEOMETRY!");
            } else {
                log.info(">>> Tabela TB_PROPRIEDADE já existe.");
            }
        } catch (Exception e) {
            log.error("Erro ao criar tabela tb_propriedade: {}", e.getMessage());
        }
    }

    private void criarTabelaPoligonos() {
        try {
            var result = entityManager.createNativeQuery(
                    "SELECT table_name FROM user_tables WHERE table_name = 'TB_POLIGONOS'"
            ).getResultList();

            if (result.isEmpty()) {
                entityManager.createNativeQuery(
                        "CREATE TABLE TB_POLIGONOS (" +
                                "  id RAW(16) NOT NULL," +
                                "  geometria SDO_GEOMETRY," +
                                "  area_hectares NUMBER(19,2)," +
                                "  propriedade_id RAW(16) NOT NULL," +
                                "  CONSTRAINT pk_poligonos PRIMARY KEY (id)" +
                                ")"
                ).executeUpdate();
                log.info(">>> Tabela TB_POLIGONOS criada!");
            } else {
                log.info(">>> Tabela TB_POLIGONOS já existe.");
            }
        } catch (Exception e) {
            log.error("Erro ao criar tabela tb_poligonos: {}", e.getMessage());
        }
    }

    private void inserirDadosSeed() {
        if (userRepository.findByEmail("farmer@carbontrack.com").isPresent()) {
            log.info(">>> Dados de seed já existem.");
            return;
        }

        log.info(">>> Inserindo dados de seed...");

        try {
            // 1. Criar usuário
            User user = userRepository.save(User.builder()
                    .nome("Carbon Farmer")
                    .email("farmer@carbontrack.com")
                    .senha("123456")
                    .build());
            log.info("✅ Usuário criado: {}", user.getNome());

            // 2. Criar geometria (polígono) a partir do WKT
            String wkt = "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, -46.6433 -23.5605, -46.6433 -23.5505, -46.6333 -23.5505))";
            Polygon geometria = converterWktParaPolygon(wkt);

            if (geometria == null) {
                log.error("Falha ao converter geometria WKT");
                return;
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

            log.info(">>> Seed inserido com sucesso!");

        } catch (Exception e) {
            log.error(">>> Erro ao inserir dados de seed: ", e);
        }
    }

    /**
     * Converte WKT (Well-Known Text) para Polygon JTS
     */
    private Polygon converterWktParaPolygon(String wkt) {
        try {
            WKTReader reader = new WKTReader(geometryFactory);
            Geometry geometry = reader.read(wkt);
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

        // Área em graus quadrados, convertendo para hectares (aproximado)
        double areaEmGraus = polygon.getArea();
        double areaHectares = areaEmGraus * 10000;

        return Math.abs(areaHectares);
    }

    private Double calcularEstoque(String wkt) {
        try {
            Geometry geometry = new WKTReader().read(wkt);
            return calculadoraService.calcularEstoque(geometry);
        } catch (Exception e) {
            log.error("Erro ao converter WKT: {}", e.getMessage());
            return 0.0;
        }
    }
}