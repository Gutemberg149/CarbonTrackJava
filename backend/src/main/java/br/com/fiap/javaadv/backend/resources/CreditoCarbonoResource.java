package br.com.fiap.javaadv.backend.resources;

import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoResponseDTO;
import br.com.fiap.javaadv.backend.services.CreditoCarbonoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/creditos")
@RequiredArgsConstructor
@Tag(name = "Créditos de Carbono", description = "Endpoints para gerenciamento de créditos de carbono")
public class CreditoCarbonoResource {

    private final CreditoCarbonoService service;

    @PostMapping
    @Operation(summary = "Registrar novo crédito", description = "Cria um novo crédito de carbono para uma propriedade")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Crédito criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<CreditoCarbonoResponseDTO> registrar(@Valid @RequestBody CreditoCarbonoRequestDTO req) {
        log.info("📝 POST /creditos - Iniciando registro de crédito para propriedade ID: {}", req.propriedadeId());

        try {
            CreditoCarbonoResponseDTO saved = service.registrar(req);
            log.info("✅ POST /creditos - Crédito criado com sucesso. ID: {}, Quantidade: {} tCO₂",
                    saved.id(), saved.quantidade());

            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(saved.id())
                    .toUri();

            return ResponseEntity.created(uri).body(saved);

        } catch (RuntimeException e) {
            log.error("❌ POST /creditos - Erro ao criar crédito: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    @Operation(summary = "Listar todos os créditos", description = "Retorna uma lista com todos os créditos de carbono cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "204", description = "Nenhum crédito encontrado")
    })
    public ResponseEntity<List<CreditoCarbonoResponseDTO>> findAll() {
        log.info("📋 GET /creditos - Buscando todos os créditos de carbono");

        List<CreditoCarbonoResponseDTO> creditos = service.listarTodos();

        if (creditos.isEmpty()) {
            log.info("📋 GET /creditos - Nenhum crédito encontrado");
            return ResponseEntity.noContent().build();
        }

        log.info("✅ GET /creditos - Encontrados {} crédito(s)", creditos.size());
        creditos.forEach(credito ->
                log.debug("   - ID: {}, Propriedade: {}, Quantidade: {} tCO₂",
                        credito.id(), credito.nomePropriedade(), credito.quantidade())
        );

        return ResponseEntity.ok(creditos);
    }

    @GetMapping("/propriedade/{propriedadeId}")
    @Operation(summary = "Buscar créditos por propriedade", description = "Retorna todos os créditos de uma propriedade específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<List<CreditoCarbonoResponseDTO>> findByPropriedade(@PathVariable UUID propriedadeId) {
        log.info("🔍 GET /creditos/propriedade/{} - Buscando créditos por propriedade", propriedadeId);

        List<CreditoCarbonoResponseDTO> creditos = service.listarPorPropriedade(propriedadeId);

        if (creditos.isEmpty()) {
            log.info("📋 GET /creditos/propriedade/{} - Nenhum crédito encontrado para esta propriedade", propriedadeId);
        } else {
            log.info("✅ GET /creditos/propriedade/{} - Encontrados {} crédito(s)", propriedadeId, creditos.size());
        }

        return ResponseEntity.ok(creditos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar crédito por ID", description = "Retorna os detalhes de um crédito de carbono específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Crédito encontrado"),
            @ApiResponse(responseCode = "404", description = "Crédito não encontrado")
    })
    public ResponseEntity<CreditoCarbonoResponseDTO> getById(@PathVariable UUID id) {
        log.info("🔍 GET /creditos/{} - Buscando crédito por ID", id);

        CreditoCarbonoResponseDTO credito = service.buscarPorId(id);

        log.info("✅ GET /creditos/{} - Crédito encontrado: Propriedade '{}', Quantidade {} tCO₂",
                id, credito.nomePropriedade(), credito.quantidade());

        return ResponseEntity.ok(credito);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar crédito", description = "Remove um crédito de carbono do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Crédito deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Crédito não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("🗑️ DELETE /creditos/{} - Solicitando deleção de crédito", id);

        service.deletar(id);

        log.info("✅ DELETE /creditos/{} - Crédito deletado com sucesso", id);
        return ResponseEntity.noContent().build();
    }
}