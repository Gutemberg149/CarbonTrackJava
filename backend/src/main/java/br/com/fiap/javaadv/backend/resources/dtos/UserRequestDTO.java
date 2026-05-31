package br.com.fiap.javaadv.backend.resources.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @Email(message = "E-mail inválido")
        @NotBlank(message = "O e-mail é obrigatório")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
        String senha
) {
    // Construtor compacto para sanitização
    public UserRequestDTO {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }
}
