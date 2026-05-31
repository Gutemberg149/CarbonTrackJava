package br.com.fiap.javaadv.backend.resources;

import br.com.fiap.javaadv.backend.resources.dtos.PropriedadeRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.PropriedadeResponseDTO;
import br.com.fiap.javaadv.backend.services.PropriedadeService;
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
@RequestMapping("/propriedades")
@RequiredArgsConstructor
@Tag(name = "Propriedades", description = "Endpoints para gerenciamento de propriedades rurais")
public class PropriedadeResource {

    private final PropriedadeService service;

    @PostMapping
    @Operation(summary = "Criar uma nova propriedade", description = "Cadastra uma nova propriedade no sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Propriedade criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<PropriedadeResponseDTO> create(@Valid @RequestBody PropriedadeRequestDTO req) {
        log.info("POST /propriedades - Recebendo requisição para criar propriedade: {}", req.getNome());

        PropriedadeResponseDTO saved = service.salvar(req);

        // Para RECORD: use saved.id() em vez de saved.getId()
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.id())  // ← CORRIGIDO: .id() não .getId()
                .toUri();

        log.info("Propriedade criada com sucesso. ID: {}", saved.id());  // ← CORRIGIDO
        return ResponseEntity.created(uri).body(saved);
    }

    @GetMapping
    @Operation(summary = "Listar todas as propriedades", description = "Retorna uma lista com todas as propriedades cadastradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<PropriedadeResponseDTO>> findAll() {
        log.info("GET /propriedades - Listando todas as propriedades");
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar propriedade por ID", description = "Retorna os detalhes de uma propriedade específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propriedade encontrada"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<PropriedadeResponseDTO> getById(@PathVariable UUID id) {
        log.info("GET /propriedades/{} - Buscando propriedade", id);
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar propriedade", description = "Atualiza os dados de uma propriedade existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propriedade atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<PropriedadeResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody PropriedadeRequestDTO req) {
        log.info("PUT /propriedades/{} - Atualizando propriedade", id);
        return ResponseEntity.ok(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar propriedade", description = "Remove uma propriedade do sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Propriedade deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("DELETE /propriedades/{} - Deletando propriedade", id);
        service.deletar(id);
        log.info("Propriedade deletada com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}