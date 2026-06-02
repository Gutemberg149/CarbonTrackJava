package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.CreditoCarbonoRepository;
import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.CreditoCarbono;
import br.com.fiap.javaadv.backend.domainmodel.entities.Propriedade;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditoCarbonoService {

    private final CreditoCarbonoRepository creditoCarbonoRepository;
    private final PropriedadeRepository propRepository;
    private final CalculadoraCarbonoService calculadoraService;

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
     * Calcula a quantidade de carbono com base na geometria e tempo de posse da propriedade
     */
    private Double calcularQuantidadeCarbono(Propriedade propriedade) {
        Double quantidadeCalculada = null;

        // Verificar se tem geometria (agora é String WKT)
        if (propriedade.getGeometria() != null && !propriedade.getGeometria().isEmpty()) {
            try {
                // Converter String WKT para Geometry
                WKTReader reader = new WKTReader();
                Geometry geometry = reader.read(propriedade.getGeometria());

                // Usar o método que considera ano e mês de aquisição
                quantidadeCalculada = calculadoraService.calcularEstoquePorTempo(
                        geometry,
                        propriedade.getAnoAquisicao(),
                        propriedade.getMesAquisicao());
                log.info("Calculado via geometria WKT e tempo de posse: {} tCO2", quantidadeCalculada);
            } catch (Exception e) {
                log.warn("Erro ao converter geometria WKT: {}", e.getMessage());
            }
        }

        // Fallback: usar área da propriedade se disponível
        if (quantidadeCalculada == null && propriedade.getAreaHectares() != null && propriedade.getAreaHectares() > 0) {
            // Fórmula aproximada: área * sequestro mensal * meses de posse
            long mesesPosse = calcularMesesPosse(propriedade.getAnoAquisicao(), propriedade.getMesAquisicao());
            double sequestroMensal = 0.5; // tCO2 por hectare por mês
            quantidadeCalculada = propriedade.getAreaHectares() * sequestroMensal * mesesPosse;
            log.info("Calculado via área: {} hectares * {} meses = {} tCO2",
                    propriedade.getAreaHectares(), mesesPosse, quantidadeCalculada);
        }

        // Valor padrão mínimo
        if (quantidadeCalculada == null || quantidadeCalculada <= 0) {
            quantidadeCalculada = 100.0;
            log.warn("Usando valor padrão: {} tCO2", quantidadeCalculada);
        }

        return quantidadeCalculada;
    }

    /**
     * Calcula a quantidade de meses entre ano/mês de aquisição e hoje
     */
    private long calcularMesesPosse(Integer anoAquisicao, Integer mesAquisicao) {
        if (anoAquisicao == null || mesAquisicao == null) {
            return 0;
        }

        try {
            java.time.YearMonth dataAquisicao = java.time.YearMonth.of(anoAquisicao, mesAquisicao);
            java.time.YearMonth hoje = java.time.YearMonth.now();

            if (dataAquisicao.isAfter(hoje)) {
                return 0;
            }

            return java.time.temporal.ChronoUnit.MONTHS.between(dataAquisicao, hoje);
        } catch (Exception e) {
            log.error("Erro ao calcular meses de posse: {}", e.getMessage());
            return 0;
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