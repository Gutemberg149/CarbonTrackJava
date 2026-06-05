package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.CreditoCarbonoRepository;
import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.CreditoCarbono;
import br.com.fiap.javaadv.backend.domainmodel.entities.Propriedade;
import br.com.fiap.javaadv.backend.integration.PlanetApiClient;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditoCarbonoService {

    private final CreditoCarbonoRepository creditoCarbonoRepository;
    private final PropriedadeRepository propRepository;
    private final CalculadoraCarbonoService calculadoraService;
    private final PlanetApiClient planetApiClient;  // ✅ ADICIONADO

    @Transactional
    public CreditoCarbonoResponseDTO registrar(CreditoCarbonoRequestDTO dto) {
        var prop = propRepository.findById(dto.propriedadeId())
                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada: " + dto.propriedadeId()));

        Double quantidadeCalculada = calcularQuantidadeCarbono(prop);

        log.info("Registrando crédito de {} tCO2 para propriedade: {}", quantidadeCalculada, prop.getNome());

        var credito = new CreditoCarbono(quantidadeCalculada, prop);
        return toResponseDTO(creditoCarbonoRepository.save(credito));
    }

    /**
     * Calcula a quantidade de carbono com dados REAIS do satélite
     */
    private Double calcularQuantidadeCarbono(Propriedade propriedade) {
        log.info("========================================");
        log.info("🔍 Calculando carbono para propriedade: {}", propriedade.getNome());
        log.info("========================================");

        // ==========================================
        // PRIORIDADE 1: Dados REAIS do satélite
        // ==========================================
        if (propriedade.getGeometria() != null && !propriedade.getGeometria().isEmpty()) {
            try {
                log.info("🛰️ Tentando obter dados REAIS do satélite Planet Labs...");

                // Converter String WKT para Geometry
                WKTReader reader = new WKTReader();
                Geometry geometry = reader.read(propriedade.getGeometria());

                // Chamar API real do satélite
                Map<String, Object> dadosSatelite = planetApiClient.calcularBiomassa(geometry);

                // Verificar se veio dados reais (não mock e não erro)
                Boolean isMock = (Boolean) dadosSatelite.getOrDefault("mock", false);
                Boolean isErro = (Boolean) dadosSatelite.getOrDefault("erro", false);

                if (!isMock && !isErro && dadosSatelite.containsKey("carbonoTotalTon")) {
                    Double carbonoReal = (Double) dadosSatelite.get("carbonoTotalTon");
                    if (carbonoReal != null && carbonoReal > 0) {
                        log.info("✅✅✅ Dados REAIS do satélite obtidos com sucesso!");
                        log.info("   🌿 Carbono total: {} tCO₂", String.format("%.2f", carbonoReal));
                        log.info("   📐 Área: {} hectares", dadosSatelite.get("areaHectares"));
                        log.info("   🛰️ Fonte: {}", dadosSatelite.get("fonte"));
                        log.info("   📊 Método: {}", dadosSatelite.get("metodo"));
                        return carbonoReal;
                    }
                }

                log.warn("⚠️ Satélite retornou dados inválidos ou mock");

            } catch (Exception e) {
                log.error("❌ Erro ao consultar satélite: {}", e.getMessage());
                log.warn("⚠️ Usando método alternativo de cálculo");
            }
        }

        // ==========================================
        // PRIORIDADE 2: Cálculo por tempo de posse
        // ==========================================
        if (propriedade.getGeometria() != null && !propriedade.getGeometria().isEmpty()) {
            try {
                WKTReader reader = new WKTReader();
                Geometry geometry = reader.read(propriedade.getGeometria());

                Double quantidadeCalculada = calculadoraService.calcularEstoquePorTempo(
                        geometry,
                        propriedade.getAnoAquisicao(),
                        propriedade.getMesAquisicao());

                if (quantidadeCalculada != null && quantidadeCalculada > 1.0) {
                    log.info("📊 Calculado via tempo de posse: {} tCO₂", String.format("%.2f", quantidadeCalculada));
                    return quantidadeCalculada;
                }
            } catch (Exception e) {
                log.warn("Erro ao converter geometria WKT: {}", e.getMessage());
            }
        }

        // ==========================================
        // PRIORIDADE 3: Cálculo por área
        // ==========================================
        if (propriedade.getAreaHectares() != null && propriedade.getAreaHectares() > 0) {
            long mesesPosse = calcularMesesPosse(propriedade.getAnoAquisicao(), propriedade.getMesAquisicao());

            // Fórmula: área * sequestro mensal * meses de posse
            double sequestroMensal = 0.5; // tCO2 por hectare por mês
            double quantidadeCalculada = propriedade.getAreaHectares() * sequestroMensal * Math.max(mesesPosse, 1);

            // Garantir valor mínimo realista
            quantidadeCalculada = Math.max(quantidadeCalculada, propriedade.getAreaHectares() * 10);

            log.info("📊 Calculado via área: {} hectares → {} tCO₂",
                    String.format("%.2f", propriedade.getAreaHectares()),
                    String.format("%.2f", quantidadeCalculada));
            return quantidadeCalculada;
        }

        // ==========================================
        // PRIORIDADE 4: Valor padrão mínimo
        // ==========================================
        Double valorMinimo = 100.0;
        log.warn("⚠️ Nenhum método disponível, usando valor mínimo: {} tCO₂", valorMinimo);
        return valorMinimo;
    }

    /**
     * Calcula a quantidade de meses entre ano/mês de aquisição e hoje
     */
    private long calcularMesesPosse(Integer anoAquisicao, Integer mesAquisicao) {
        if (anoAquisicao == null || mesAquisicao == null) {
            return 12; // Retorna 12 meses (1 ano) como padrão
        }

        try {
            java.time.YearMonth dataAquisicao = java.time.YearMonth.of(anoAquisicao, mesAquisicao);
            java.time.YearMonth hoje = java.time.YearMonth.now();

            if (dataAquisicao.isAfter(hoje)) {
                return 1;
            }

            long meses = java.time.temporal.ChronoUnit.MONTHS.between(dataAquisicao, hoje);
            return Math.max(meses, 1);
        } catch (Exception e) {
            log.error("Erro ao calcular meses de posse: {}", e.getMessage());
            return 12;
        }
    }

    @Transactional(readOnly = true)
    public List<CreditoCarbonoResponseDTO> listarTodos() {
        return creditoCarbonoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CreditoCarbonoResponseDTO> listarPorPropriedade(UUID propriedadeId) {
        return creditoCarbonoRepository.findByPropriedadeId(propriedadeId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CreditoCarbonoResponseDTO buscarPorId(UUID id) {
        return creditoCarbonoRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Crédito não encontrado: " + id));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!creditoCarbonoRepository.existsById(id)) {
            throw new RuntimeException("Crédito não encontrado: " + id);
        }
        creditoCarbonoRepository.deleteById(id);
        log.info("Crédito deletado: {}", id);
    }

    private CreditoCarbonoResponseDTO toResponseDTO(CreditoCarbono c) {
        return new CreditoCarbonoResponseDTO(
                c.getId(),
                c.getQuantidade(),
                c.getDataEmissao(),
                c.getPropriedade().getNome()
        );
    }
}