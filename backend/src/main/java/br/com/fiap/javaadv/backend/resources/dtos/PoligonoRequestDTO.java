package br.com.fiap.javaadv.backend.resources.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de requisição do polígono")
public class PoligonoRequestDTO {

    @Schema(description = "Geometria em formato GeoJSON",
            example = "{\"type\":\"Polygon\",\"coordinates\":[[[-46.6333,-23.5505],[-46.6340,-23.5510],[-46.6330,-23.5515],[-46.6333,-23.5505]]]}")
    @NotNull(message = "A geometria é obrigatória")
    private String geometriaGeoJson;

    @Schema(description = "ID da propriedade", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    @NotNull(message = "O ID da propriedade é obrigatório")
    private UUID propriedadeId;

    @Schema(description = "Área em hectares (opcional - será calculada automaticamente)")
    private Double areaHectares;
}