package br.com.fiap.javaadv.backend.resources.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @Email(message = "E-mail inválido")
        @NotBlank(message = "O e-mail é obrigatório")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
        String senha
) {

    public LoginRequestDTO {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    // MÉTODO DE CONVENIÊNCIA:
    public String toLogString() {
        return "Login attempt for: " + email;
    }
}


