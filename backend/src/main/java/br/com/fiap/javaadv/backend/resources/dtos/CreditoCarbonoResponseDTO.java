package br.com.fiap.javaadv.backend.resources.dtos;

import java.util.UUID;

public record CreditoCarbonoResponseDTO(
        UUID id,
        Double quantidade,
        String dataEmissao,
        String nomePropriedade
) {}