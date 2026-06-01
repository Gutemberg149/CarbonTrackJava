//package br.com.fiap.javaadv.backend.resources.dtos;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Positive;
//
//import java.util.UUID;
//
//public record CreditoCarbonoRequestDTO(
//        @NotNull @Positive Double quantidade,
//        @NotNull UUID propriedadeId
//) {}

package br.com.fiap.javaadv.backend.resources.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreditoCarbonoRequestDTO(
        @NotNull(message = "O ID da propriedade é obrigatório")
        UUID propriedadeId
) {}