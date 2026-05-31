package br.com.fiap.javaadv.backend.resources.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreditoCarbonoResponseDTO (
        UUID id,
        Double quantidade,
        LocalDateTime dataEmissao,
        String nomePropriedade
){
}
