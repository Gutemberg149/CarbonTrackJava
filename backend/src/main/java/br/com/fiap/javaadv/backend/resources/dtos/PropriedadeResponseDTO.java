package br.com.fiap.javaadv.backend.resources.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO de resposta da propriedade")
public record PropriedadeResponseDTO(
        @Schema(description = "ID único da propriedade")
        UUID id,

        @Schema(description = "Nome da propriedade")
        String nome,

        @Schema(description = "Endereço completo")
        String endereco,

        @Schema(description = "Cidade")
        String cidade,

        @Schema(description = "Estado")
        String estado,

        @Schema(description = "CEP")
        String cep,

        @Schema(description = "Área em hectares")
        Double areaHectares,

        @Schema(description = "Carbono sequestrado estimado")
        Double carbonoEstimado,

        @Schema(description = "Coordenadas em formato JSON")
        String coordenadasJson,

        @Schema(description = "Geometria em formato WKT")
        String geometriaWkt,

        @Schema(description = "Geometria em formato GeoJSON")
        Map<String, Object> geometriaGeoJson,

        @Schema(description = "ID do proprietário")
        UUID donoId,

        @Schema(description = "Nome do proprietário")
        String donoNome,

        @Schema(description = "Email do proprietário")
        String donoEmail
) {
    // Método builder manual (opcional)
    public static PropriedadeResponseDTOBuilder builder() {
        return new PropriedadeResponseDTOBuilder();
    }

    // Builder manual
    public static class PropriedadeResponseDTOBuilder {
        private UUID id;
        private String nome;
        private String endereco;
        private String cidade;
        private String estado;
        private String cep;
        private Double areaHectares;
        private Double carbonoEstimado;
        private String coordenadasJson;
        private String geometriaWkt;
        private Map<String, Object> geometriaGeoJson;
        private UUID donoId;
        private String donoNome;
        private String donoEmail;

        public PropriedadeResponseDTOBuilder id(UUID id) { this.id = id; return this; }
        public PropriedadeResponseDTOBuilder nome(String nome) { this.nome = nome; return this; }
        public PropriedadeResponseDTOBuilder endereco(String endereco) { this.endereco = endereco; return this; }
        public PropriedadeResponseDTOBuilder cidade(String cidade) { this.cidade = cidade; return this; }
        public PropriedadeResponseDTOBuilder estado(String estado) { this.estado = estado; return this; }
        public PropriedadeResponseDTOBuilder cep(String cep) { this.cep = cep; return this; }
        public PropriedadeResponseDTOBuilder areaHectares(Double areaHectares) { this.areaHectares = areaHectares; return this; }
        public PropriedadeResponseDTOBuilder carbonoEstimado(Double carbonoEstimado) { this.carbonoEstimado = carbonoEstimado; return this; }
        public PropriedadeResponseDTOBuilder coordenadasJson(String coordenadasJson) { this.coordenadasJson = coordenadasJson; return this; }
        public PropriedadeResponseDTOBuilder geometriaWkt(String geometriaWkt) { this.geometriaWkt = geometriaWkt; return this; }
        public PropriedadeResponseDTOBuilder geometriaGeoJson(Map<String, Object> geometriaGeoJson) { this.geometriaGeoJson = geometriaGeoJson; return this; }
        public PropriedadeResponseDTOBuilder donoId(UUID donoId) { this.donoId = donoId; return this; }
        public PropriedadeResponseDTOBuilder donoNome(String donoNome) { this.donoNome = donoNome; return this; }
        public PropriedadeResponseDTOBuilder donoEmail(String donoEmail) { this.donoEmail = donoEmail; return this; }

        public PropriedadeResponseDTO build() {
            return new PropriedadeResponseDTO(
                    id, nome, endereco, cidade, estado, cep,
                    areaHectares, carbonoEstimado, coordenadasJson,
                    geometriaWkt, geometriaGeoJson,
                    donoId, donoNome, donoEmail
            );
        }
    }
}