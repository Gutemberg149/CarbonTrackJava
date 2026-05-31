package br.com.fiap.javaadv.backend.resources.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta do polígono")
public class PoligonoResponseDTO {

    @Schema(description = "ID do polígono")
    private UUID id;

    @Schema(description = "ID da propriedade")
    private UUID propriedadeId;

    @Schema(description = "Nome da propriedade")
    private String propriedadeNome;

    @Schema(description = "Geometria em formato GeoJSON")
    private Map<String, Object> geometriaGeoJson;

    @Schema(description = "Geometria em formato WKT")
    private String geometriaWkt;

    @Schema(description = "Área em hectares")
    private Double areaHectares;

    @Schema(description = "Data de criação")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data da última atualização")
    private LocalDateTime dataAtualizacao;
}
