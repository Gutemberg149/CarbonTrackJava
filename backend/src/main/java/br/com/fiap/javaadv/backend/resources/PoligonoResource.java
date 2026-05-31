package br.com.fiap.javaadv.backend.resources;

import br.com.fiap.javaadv.backend.resources.dtos.PoligonoRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.PoligonoResponseDTO;
import br.com.fiap.javaadv.backend.services.PoligonoService;
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
@RequestMapping("/poligonos")
@RequiredArgsConstructor
@Tag(name = "Polígonos", description = "Endpoints para gerenciamento de polígonos das propriedades")
public class PoligonoResource {

    private final PoligonoService service;

    @PostMapping
    @Operation(summary = "Criar um novo polígono")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Polígono criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Propriedade não encontrada")
    })
    public ResponseEntity<PoligonoResponseDTO> create(@Valid @RequestBody PoligonoRequestDTO request) {
        log.info("POST /poligonos - Criando novo polígono");
        PoligonoResponseDTO saved = service.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(uri).body(saved);
    }

    @GetMapping
    @Operation(summary = "Listar todos os polígonos")
    public ResponseEntity<List<PoligonoResponseDTO>> findAll() {
        log.info("GET /poligonos - Listando todos os polígonos");
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar polígono por ID")
    public ResponseEntity<PoligonoResponseDTO> getById(@PathVariable UUID id) {
        log.info("GET /poligonos/{} - Buscando polígono", id);
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/propriedade/{propriedadeId}")
    @Operation(summary = "Buscar polígonos por propriedade")
    public ResponseEntity<List<PoligonoResponseDTO>> getByPropriedade(@PathVariable UUID propriedadeId) {
        log.info("GET /poligonos/propriedade/{} - Buscando polígonos da propriedade", propriedadeId);
        return ResponseEntity.ok(service.buscarPorPropriedade(propriedadeId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar polígono")
    public ResponseEntity<PoligonoResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody PoligonoRequestDTO request) {
        log.info("PUT /poligonos/{} - Atualizando polígono", id);
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar polígono")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("DELETE /poligonos/{} - Deletando polígono", id);
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
