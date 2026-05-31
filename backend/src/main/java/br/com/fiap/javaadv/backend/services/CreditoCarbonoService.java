//package br.com.fiap.javaadv.backend.services;
//
//import br.com.fiap.javaadv.backend.datasource.repositories.CreditoCarbonoRepository;
//import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
//import br.com.fiap.javaadv.backend.domainmodel.entities.CreditoCarbono;
//import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoRequestDTO;
//import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoResponseDTO;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.locationtech.jts.geom.Geometry;
//import org.locationtech.jts.io.WKTReader;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CreditoCarbonoService {
//
//    private final CreditoCarbonoRepository creditoCarbonoRepository;
//    private final PropriedadeRepository propRepository;
//    private final CalculadoraCarbonoService calculadoraService;
//
//    @Transactional
//    public CreditoCarbonoResponseDTO registrar(CreditoCarbonoRequestDTO dto) {
//        var prop = propRepository.findById(dto.propriedadeId())
//                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada: " + dto.propriedadeId()));
//
//        Double quantidadeCalculada;
//
//        // Tenta calcular a quantidade baseada na geometria
//        if (prop.getGeometria() != null && !prop.getGeometria().isBlank()) {
//            log.info("Calculando carbono a partir da geometria WKT");
//            quantidadeCalculada = calculadoraService.calcularEstoque(prop.getGeometria());
//        } else {
//            // Fallback: usar a área em hectares se disponível
//            if (prop.getAreaHectares() != null && prop.getAreaHectares() > 0) {
//                log.info("Geometria não disponível, usando área para cálculo aproximado: {} hectares", prop.getAreaHectares());
//                // Cálculo aproximado baseado na área
//                quantidadeCalculada = prop.getAreaHectares() * 150.0 * 0.47;
//            } else {
//                // Último fallback: usar o valor enviado no DTO
//                log.warn("Sem geometria e sem área definida, usando quantidade do request");
//                quantidadeCalculada = dto.quantidade();
//            }
//        }
//
//        // Garantir que a quantidade seja positiva
//        if (quantidadeCalculada == null || quantidadeCalculada <= 0) {
//            log.warn("Quantidade calculada inválida ({}), usando valor do request: {}", quantidadeCalculada, dto.quantidade());
//            quantidadeCalculada = dto.quantidade();
//        }
//
//        // Último recurso: valor padrão
//        if (quantidadeCalculada == null || quantidadeCalculada <= 0) {
//            log.warn("Usando valor padrão de 100 tCO2");
//            quantidadeCalculada = 100.0;
//        }
//
//        log.info("Registrando crédito de {} tCO2 para propriedade: {}", quantidadeCalculada, prop.getNome());
//
//        var credito = new CreditoCarbono(quantidadeCalculada, prop);
//        return toResponseDTO(creditoCarbonoRepository.save(credito));
//    }
//
//    @Transactional(readOnly = true)
//    public List<CreditoCarbonoResponseDTO> listarTodos() {
//        return creditoCarbonoRepository.findAll().stream()
//                .map(this::toResponseDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public List<CreditoCarbonoResponseDTO> listarPorPropriedade(UUID propriedadeId) {
//        return creditoCarbonoRepository.findByPropriedadeId(propriedadeId).stream()
//                .map(this::toResponseDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional(readOnly = true)
//    public CreditoCarbonoResponseDTO buscarPorId(UUID id) {
//        return creditoCarbonoRepository.findById(id)
//                .map(this::toResponseDTO)
//                .orElseThrow(() -> new RuntimeException("Crédito não encontrado: " + id));
//    }
//
//    @Transactional
//    public void deletar(UUID id) {
//        if (!creditoCarbonoRepository.existsById(id)) {
//            throw new RuntimeException("Crédito não encontrado: " + id);
//        }
//        creditoCarbonoRepository.deleteById(id);
//        log.info("Crédito deletado: {}", id);
//    }
//
//    private CreditoCarbonoResponseDTO toResponseDTO(CreditoCarbono c) {
//        return new CreditoCarbonoResponseDTO(
//                c.getId(),
//                c.getQuantidade(),
//                c.getDataEmissao(),
//                c.getPropriedade().getNome()
//        );
//    }
//}

package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.CreditoCarbonoRepository;
import br.com.fiap.javaadv.backend.datasource.repositories.PropriedadeRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.CreditoCarbono;
import br.com.fiap.javaadv.backend.domainmodel.entities.Propriedade;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Polygon;
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
        Propriedade prop = propRepository.findById(dto.propriedadeId())
                .orElseThrow(() -> new RuntimeException("Propriedade não encontrada: " + dto.propriedadeId()));

        Double quantidadeCalculada = calcularQuantidadeCarbono(prop, dto);

        log.info("Registrando crédito de {} tCO2 para propriedade: {}", quantidadeCalculada, prop.getNome());

        CreditoCarbono credito = new CreditoCarbono(quantidadeCalculada, prop);
        return toResponseDTO(creditoCarbonoRepository.save(credito));
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

    /**
     * Calcula a quantidade de carbono com base na geometria ou área da propriedade
     */
    private Double calcularQuantidadeCarbono(Propriedade propriedade, CreditoCarbonoRequestDTO dto) {
        Polygon geometria = propriedade.getGeometria();
        Double areaHectares = propriedade.getAreaHectares();

        // 1. Prioridade: calcular a partir da geometria
        if (geometria != null && !geometria.isEmpty()) {
            log.info("Calculando carbono a partir da geometria da propriedade");
            return calculadoraService.calcularEstoque(geometria);
        }

        // 2. Fallback: usar a área em hectares se disponível
        if (areaHectares != null && areaHectares > 0) {
            log.info("Geometria não disponível, usando área para cálculo aproximado: {} hectares", areaHectares);
            // Cálculo aproximado baseado na área (150 toneladas de biomassa por hectare * 0.47 fator carbono)
            return areaHectares * 150.0 * 0.47;
        }

        // 3. Último fallback: usar o valor enviado no DTO
        if (dto.quantidade() != null && dto.quantidade() > 0) {
            log.warn("Sem geometria e sem área definida, usando quantidade do request: {}", dto.quantidade());
            return dto.quantidade();
        }

        // 4. Valor padrão mínimo
        log.warn("Usando valor padrão de 100 tCO₂");
        return 100.0;
    }

    private CreditoCarbonoResponseDTO toResponseDTO(CreditoCarbono credito) {
        return new CreditoCarbonoResponseDTO(
                credito.getId(),
                credito.getQuantidade(),
                credito.getDataEmissao(),
                credito.getPropriedade().getNome()
        );
    }
}