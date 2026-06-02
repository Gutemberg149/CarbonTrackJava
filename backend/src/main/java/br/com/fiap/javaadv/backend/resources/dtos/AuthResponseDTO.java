package br.com.fiap.javaadv.backend.resources.dtos;

public record AuthResponseDTO(
        String token,
        String tipo,
        String email,
        String nome
) {}