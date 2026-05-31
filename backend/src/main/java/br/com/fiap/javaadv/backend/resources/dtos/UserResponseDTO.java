package br.com.fiap.javaadv.backend.resources.dtos;

import java.util.UUID;

/**
 * DTO para resposta de usuários.
 * O uso de 'record' elimina a necessidade de getters,
 * setters, equals, hashCode e toString manuais.
 */
public record UserResponseDTO(
        UUID id,
        String nome,
        String email
) {
    public UserResponseDTO {
        // Validação: garante que o nome não seja nulo ou vazio
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome não pode ser vazio");
        }
        // O record automaticamente atribui os valores aos campos após este bloco
    }
}