package br.com.fiap.javaadv.backend.resources.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreditoCarbonoRequestDTO(
        @NotNull(message = "O ID da propriedade é obrigatório")
        UUID propriedadeId
) {}