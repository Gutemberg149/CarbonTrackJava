package br.com.fiap.javaadv.backend.infrastructure;

import br.com.fiap.javaadv.backend.datasource.repositories.*;
import br.com.fiap.javaadv.backend.domainmodel.embeddables.Endereco;
import br.com.fiap.javaadv.backend.domainmodel.entities.*;
import br.com.fiap.javaadv.backend.integration.PlanetApiClient;
import br.com.fiap.javaadv.backend.services.CalculadoraCarbonoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataLoader {

    private final UserRepository userRepository;
    private final PropriedadeRepository propriedadeRepository;
    private final CreditoCarbonoRepository creditoRepository;
    private final CalculadoraCarbonoService calculadoraService;
    private final PlanetApiClient planetApiClient;  // ✅ ADICIONADO

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("========================================");
            log.info("🚀 Inicializando DataLoader...");
            log.info("========================================");
            inserirDadosSeed();
        };
    }

    private void inserirDadosSeed() {
        // Verificar se já existem dados
        if (userRepository.count() > 0) {
            log.info("📊 Dados de seed já existem. Total de usuários: {}", userRepository.count());
            return;
        }

        log.info("🌱 Inserindo dados de seed pela primeira vez...");

        try {
            // ============================================
            // 1. Criar usuário padrão
            // ============================================
            User user = User.builder()
                    .nome("Carbon Farmer")
                    .email("farmer@carbontrack.com")
                    .senha("123456")
                    .build();
            user = userRepository.save(user);
            log.info("✅ Usuário criado: {} (ID: {})", user.getNome(), user.getId());

            // ============================================
            // 2. Definir geometria (WKT)
            // ============================================
            String geometriaWkt = "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, -46.6433 -23.5605, -46.6433 -23.5505, -46.6333 -23.5505))";
            log.info("📐 Geometria definida: {}", geometriaWkt);

            // ============================================
            // 3. Converter para Polygon
            // ============================================
            Polygon polygon = converterWktParaPolygon(geometriaWkt);

            if (polygon == null) {
                log.error("❌ Falha ao converter geometria WKT");
                return;
            }

            // Definir data de aquisição (padrão: data atual)
            YearMonth hoje = YearMonth.now();
            int anoAquisicao = hoje.getYear();
            int mesAquisicao = hoje.getMonthValue();

            double areaHectares = calculadoraService.calcularAreaHectares(polygon);

            // ============================================
            // 4. OBTER CARBONO DO SATÉLITE (MODO REAL)
            // ============================================
            double carbonoEstimado = obterCarbonoDoSatelite(polygon, anoAquisicao, mesAquisicao, areaHectares);

            log.info("📊 Cálculos realizados:");
            log.info("   📐 Área: {} hectares", String.format("%.2f", areaHectares));
            log.info("   📅 Data de aquisição: {}/{}", mesAquisicao, anoAquisicao);
            log.info("   🌿 Carbono acumulado: {} tCO₂", String.format("%.2f", carbonoEstimado));

            // ============================================
            // 5. Criar objeto Endereco simplificado
            // ============================================
            Endereco endereco = Endereco.builder()
                    .endereco("Estrada Rural, 100")
                    .cidade("São Paulo")
                    .estado("SP")
                    .cep("01234567")
                    .build();

            // ============================================
            // 6. Criar propriedade
            // ============================================
            Propriedade propriedade = Propriedade.builder()
                    .nome("Fazenda Mata Atlântica")
                    .endereco(endereco)
                    .anoAquisicao(anoAquisicao)
                    .mesAquisicao(mesAquisicao)
                    .areaHectares(areaHectares)
                    .carbonoEstimado(carbonoEstimado)
                    .geometria(geometriaWkt)
                    .dono(user)
                    .build();

            propriedade = propriedadeRepository.save(propriedade);
            log.info("✅ Propriedade criada: {} (ID: {})", propriedade.getNome(), propriedade.getId());

            // ============================================
            // 7. Criar crédito de carbono
            // ============================================
            CreditoCarbono credito = CreditoCarbono.builder()
                    .quantidade(carbonoEstimado)
                    .dataEmissao(LocalDateTime.now())
                    .propriedade(propriedade)
                    .build();

            credito = creditoRepository.save(credito);
            log.info("✅ Crédito de carbono criado: {} tCO₂ (ID: {})",
                    String.format("%.2f", credito.getQuantidade()),
                    credito.getId());

            // ============================================
            // 8. Resumo final
            // ============================================
            log.info("========================================");
            log.info("🎉 SEED CONCLUÍDO COM SUCESSO!");
            log.info("========================================");
            log.info("Resumo:");
            log.info("   👤 Usuário: {}", user.getNome());
            log.info("   🏠 Propriedade: {}", propriedade.getNome());
            log.info("   📅 Aquisição: {}/{}", mesAquisicao, anoAquisicao);
            log.info("   📐 Área: {} hectares", String.format("%.2f", areaHectares));
            log.info("   🌿 Carbono acumulado: {} tCO₂", String.format("%.2f", carbonoEstimado));
            log.info("   💰 Crédito: {} tCO₂", String.format("%.2f", credito.getQuantidade()));
            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ Erro ao inserir dados de seed: ", e);
        }
    }

    /**
     * Obtém carbono do satélite (API REAL) ou fallback
     */
    private double obterCarbonoDoSatelite(Polygon polygon, int anoAquisicao, int mesAquisicao, double areaHectares) {
        log.info("🛰️ Iniciando consulta ao satélite para dados REAIS...");

        try {
            // Tentar obter dados REAIS do satélite
            Map<String, Object> dadosSatelite = planetApiClient.calcularBiomassa(polygon);

            // Verificar se veio dados reais (não mock)
            Boolean isMock = (Boolean) dadosSatelite.getOrDefault("mock", false);
            Boolean isErro = (Boolean) dadosSatelite.getOrDefault("erro", false);

            if (!isMock && !isErro && dadosSatelite.containsKey("carbonoTotalTon")) {
                Double carbonoReal = (Double) dadosSatelite.get("carbonoTotalTon");
                if (carbonoReal != null && carbonoReal > 0) {
                    log.info("✅✅✅ Dados REAIS do satélite obtidos com sucesso!");
                    log.info("   🌿 Carbono do satélite: {} tCO₂", String.format("%.2f", carbonoReal));
                    log.info("   🛰️ Fonte: {}", dadosSatelite.get("fonte"));
                    log.info("   📊 Método: {}", dadosSatelite.get("metodo"));
                    return carbonoReal;
                }
            }

            log.warn("⚠️ Satélite retornou dados inválidos ou mock, usando fallback");

        } catch (Exception e) {
            log.error("❌ Erro ao consultar satélite: {}", e.getMessage());
            log.warn("⚠️ Usando cálculo fallback");
        }


        double carbonoFallback = calculadoraService.calcularEstoquePorTempo(polygon, anoAquisicao, mesAquisicao);

        // Se fallback for muito baixo (valor mínimo), usar cálculo por área
        if (carbonoFallback <= 1.0) {
            carbonoFallback = areaHectares * 150.0 * 0.47;
            log.info("📊 Usando cálculo por área (fallback): {} tCO₂", String.format("%.2f", carbonoFallback));
        } else {
            log.info("📊 Usando cálculo por tempo de posse (fallback): {} tCO₂", String.format("%.2f", carbonoFallback));
        }

        return carbonoFallback;
    }

    /**
     * Converte WKT (Well-Known Text) para Polygon JTS
     */
    private Polygon converterWktParaPolygon(String wkt) {
        if (wkt == null || wkt.isEmpty()) {
            log.error("WKT vazio ou nulo");
            return null;
        }

        try {
            WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(wkt);

            if (geometry instanceof Polygon polygon) {
                log.debug("✅ Geometria convertida com sucesso: {}", polygon.toText());
                return polygon;
            }

            log.error("❌ A geometria não é um polígono válido. Tipo: {}", geometry.getGeometryType());
            return null;

        } catch (Exception e) {
            log.error("❌ Erro ao converter WKT para Polygon: {}", e.getMessage());
            return null;
        }
    }
}