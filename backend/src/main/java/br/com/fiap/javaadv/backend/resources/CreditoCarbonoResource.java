package br.com.fiap.javaadv.backend.resources;

import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.CreditoCarbonoResponseDTO;
import br.com.fiap.javaadv.backend.services.CreditoCarbonoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/creditos")
@RequiredArgsConstructor
@Tag(name = "Créditos de Carbono")
public class CreditoCarbonoResource {

    private final CreditoCarbonoService service;

    @PostMapping
    public ResponseEntity<CreditoCarbonoResponseDTO> registrar(@Valid @RequestBody CreditoCarbonoRequestDTO req) {
        var saved = service.registrar(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.id()).toUri();
        return ResponseEntity.created(uri).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<CreditoCarbonoResponseDTO>> findAll() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/propriedade/{propriedadeId}")
    public ResponseEntity<List<CreditoCarbonoResponseDTO>> findByPropriedade(@PathVariable UUID propriedadeId) {
        return ResponseEntity.ok(service.listarPorPropriedade(propriedadeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditoCarbonoResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}