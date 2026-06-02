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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
    public ResponseEntity<EntityModel<PropriedadeResponseDTO>> create(@Valid @RequestBody PropriedadeRequestDTO req) {
        log.info("POST /propriedades - Recebendo requisição para criar propriedade: {}", req.getNome());

        PropriedadeResponseDTO saved = service.salvar(req);

        // Usar getter em vez de id() direto
        EntityModel<PropriedadeResponseDTO> entityModel = EntityModel.of(saved);

        // Adicionar links usando getter
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).getById(saved.getId())).withSelfRel());
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).findAll()).withRel("all-properties"));
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).update(saved.getId(), null)).withRel("update"));
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).delete(saved.getId())).withRel("delete"));

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        log.info("Propriedade criada com sucesso. ID: {}", saved.getId());
        return ResponseEntity.created(uri).body(entityModel);
    }

    @GetMapping
    @Operation(summary = "Listar todas as propriedades", description = "Retorna uma lista com todas as propriedades cadastradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<CollectionModel<PropriedadeResponseDTO>> findAll() {
        log.info("GET /propriedades - Listando todas as propriedades");

        List<PropriedadeResponseDTO> propriedades = service.listarTodas();

        CollectionModel<PropriedadeResponseDTO> collectionModel = CollectionModel.of(propriedades);
        collectionModel.add(linkTo(methodOn(PropriedadeResource.class).findAll()).withSelfRel());
        collectionModel.add(linkTo(methodOn(PropriedadeResource.class).create(null)).withRel("create"));

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar propriedade por ID", description = "Retorna os detalhes de uma propriedade específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propriedade encontrada"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<EntityModel<PropriedadeResponseDTO>> getById(@PathVariable UUID id) {
        log.info("GET /propriedades/{} - Buscando propriedade", id);

        PropriedadeResponseDTO dto = service.buscarPorId(id);

        EntityModel<PropriedadeResponseDTO> entityModel = EntityModel.of(dto);
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).getById(id)).withSelfRel());
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).findAll()).withRel("all-properties"));
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).update(id, null)).withRel("update"));
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).delete(id)).withRel("delete"));

        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar propriedade", description = "Atualiza os dados de uma propriedade existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propriedade atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<EntityModel<PropriedadeResponseDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody PropriedadeRequestDTO req) {
        log.info("PUT /propriedades/{} - Atualizando propriedade", id);

        PropriedadeResponseDTO updated = service.atualizar(id, req);

        EntityModel<PropriedadeResponseDTO> entityModel = EntityModel.of(updated);
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).getById(id)).withSelfRel());
        entityModel.add(linkTo(methodOn(PropriedadeResource.class).findAll()).withRel("all-properties"));

        return ResponseEntity.ok(entityModel);
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