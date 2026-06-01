//package br.com.fiap.javaadv.backend.resources.dtos;
//
//import br.com.fiap.javaadv.backend.config.TwoDecimalSerializer;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import io.swagger.v3.oas.annotations.media.Schema;
//
//import java.util.Map;
//import java.util.UUID;
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@Schema(description = "DTO de resposta da propriedade")
//public record PropriedadeResponseDTO(
//        @Schema(description = "ID único da propriedade", example = "550e8400-e29b-41d4-a716-446655440000")
//        UUID id,
//
//        @Schema(description = "Nome da propriedade", example = "Fazenda Boa Vista")
//        String nome,
//
//        @Schema(description = "Endereço completo", example = "Rodovia BR-101, Km 50")
//        String endereco,
//
//        @Schema(description = "Cidade", example = "São Paulo")
//        String cidade,
//
//        @Schema(description = "Estado (sigla)", example = "SP")
//        String estado,
//
//        @Schema(description = "CEP", example = "01234567")
//        String cep,
//
//        @Schema(description = "Área em hectares (calculada automaticamente a partir da geometria)",
//                example = "11334.51")
//        @JsonSerialize(using = TwoDecimalSerializer.class)
//        Double areaHectares,
//
//        @Schema(description = "Carbono sequestrado estimado em toneladas (tCO₂)",
//                example = "799082.84")
//        @JsonSerialize(using = TwoDecimalSerializer.class)
//        Double carbonoEstimado,
//
//        @Schema(description = "Geometria em formato WKT (Well-Known Text)",
//                example = "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, -46.6233 -23.5605, -46.6233 -23.5505, -46.6333 -23.5505))")
//        String geometriaWkt,
//
//        @Schema(description = "Geometria em formato GeoJSON")
//        Map<String, Object> geometriaGeoJson,
//
//        @Schema(description = "ID do proprietário/dono")
//        UUID donoId,
//
//        @Schema(description = "Nome do proprietário/dono", example = "Carbon Farmer")
//        String donoNome,
//
//        @Schema(description = "Email do proprietário/dono", example = "farmer@carbontrack.com")
//        String donoEmail
//) {
//    /**
//     * Builder manual para criar instâncias de PropriedadeResponseDTO
//     */
//    public static PropriedadeResponseDTOBuilder builder() {
//        return new PropriedadeResponseDTOBuilder();
//    }
//
//    /**
//     * Builder para facilitar a criação do DTO
//     */
//    public static class PropriedadeResponseDTOBuilder {
//        private UUID id;
//        private String nome;
//        private String endereco;
//        private String cidade;
//        private String estado;
//        private String cep;
//        private Double areaHectares;
//        private Double carbonoEstimado;
//        private String geometriaWkt;
//        private Map<String, Object> geometriaGeoJson;
//        private UUID donoId;
//        private String donoNome;
//        private String donoEmail;
//
//        public PropriedadeResponseDTOBuilder id(UUID id) {
//            this.id = id;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder nome(String nome) {
//            this.nome = nome;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder endereco(String endereco) {
//            this.endereco = endereco;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder cidade(String cidade) {
//            this.cidade = cidade;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder estado(String estado) {
//            this.estado = estado;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder cep(String cep) {
//            this.cep = cep;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder areaHectares(Double areaHectares) {
//            this.areaHectares = areaHectares;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder carbonoEstimado(Double carbonoEstimado) {
//            this.carbonoEstimado = carbonoEstimado;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder geometriaWkt(String geometriaWkt) {
//            this.geometriaWkt = geometriaWkt;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder geometriaGeoJson(Map<String, Object> geometriaGeoJson) {
//            this.geometriaGeoJson = geometriaGeoJson;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder donoId(UUID donoId) {
//            this.donoId = donoId;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder donoNome(String donoNome) {
//            this.donoNome = donoNome;
//            return this;
//        }
//
//        public PropriedadeResponseDTOBuilder donoEmail(String donoEmail) {
//            this.donoEmail = donoEmail;
//            return this;
//        }
//
//        public PropriedadeResponseDTO build() {
//            // Formatar números com 2 casas decimais
//            Double areaFormatada = areaHectares != null
//                    ? Math.round(areaHectares * 100.0) / 100.0
//                    : null;
//            Double carbonoFormatado = carbonoEstimado != null
//                    ? Math.round(carbonoEstimado * 100.0) / 100.0
//                    : null;
//
//            return new PropriedadeResponseDTO(
//                    id,
//                    nome,
//                    endereco,
//                    cidade,
//                    estado,
//                    cep,
//                    areaFormatada,
//                    carbonoFormatado,
//                    geometriaWkt,
//                    geometriaGeoJson,
//                    donoId,
//                    donoNome,
//                    donoEmail
//            );
//        }
//    }
//}

package br.com.fiap.javaadv.backend.resources.dtos;

import br.com.fiap.javaadv.backend.config.TwoDecimalSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

        @Schema(description = "Estado (sigla)")
        String estado,

        @Schema(description = "CEP")
        String cep,

        @Schema(description = "Ano de aquisição")
        Integer anoAquisicao,

        @Schema(description = "Mês de aquisição")
        Integer mesAquisicao,

        @Schema(description = "Área em hectares (calculada automaticamente a partir da geometria)")
        @JsonSerialize(using = TwoDecimalSerializer.class)
        Double areaHectares,

        @Schema(description = "Carbono sequestrado estimado em toneladas (tCO₂)")
        @JsonSerialize(using = TwoDecimalSerializer.class)
        Double carbonoEstimado,

        @Schema(description = "Geometria em formato WKT (Well-Known Text)")
        String geometriaWkt,

        @Schema(description = "Geometria em formato GeoJSON")
        Map<String, Object> geometriaGeoJson,

        @Schema(description = "ID do proprietário/dono")
        UUID donoId,

        @Schema(description = "Nome do proprietário/dono")
        String donoNome,

        @Schema(description = "Email do proprietário/dono")
        String donoEmail
) {
    // Builder manual
    public static PropriedadeResponseDTOBuilder builder() {
        return new PropriedadeResponseDTOBuilder();
    }

    public static class PropriedadeResponseDTOBuilder {
        private UUID id;
        private String nome;
        private String endereco;
        private String cidade;
        private String estado;
        private String cep;
        private Integer anoAquisicao;
        private Integer mesAquisicao;
        private Double areaHectares;
        private Double carbonoEstimado;
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
        public PropriedadeResponseDTOBuilder anoAquisicao(Integer anoAquisicao) { this.anoAquisicao = anoAquisicao; return this; }
        public PropriedadeResponseDTOBuilder mesAquisicao(Integer mesAquisicao) { this.mesAquisicao = mesAquisicao; return this; }
        public PropriedadeResponseDTOBuilder areaHectares(Double areaHectares) { this.areaHectares = areaHectares; return this; }
        public PropriedadeResponseDTOBuilder carbonoEstimado(Double carbonoEstimado) { this.carbonoEstimado = carbonoEstimado; return this; }
        public PropriedadeResponseDTOBuilder geometriaWkt(String geometriaWkt) { this.geometriaWkt = geometriaWkt; return this; }
        public PropriedadeResponseDTOBuilder geometriaGeoJson(Map<String, Object> geometriaGeoJson) { this.geometriaGeoJson = geometriaGeoJson; return this; }
        public PropriedadeResponseDTOBuilder donoId(UUID donoId) { this.donoId = donoId; return this; }
        public PropriedadeResponseDTOBuilder donoNome(String donoNome) { this.donoNome = donoNome; return this; }
        public PropriedadeResponseDTOBuilder donoEmail(String donoEmail) { this.donoEmail = donoEmail; return this; }

        public PropriedadeResponseDTO build() {
            Double areaFormatada = areaHectares != null ? Math.round(areaHectares * 100.0) / 100.0 : null;
            Double carbonoFormatado = carbonoEstimado != null ? Math.round(carbonoEstimado * 100.0) / 100.0 : null;

            return new PropriedadeResponseDTO(
                    id, nome, endereco, cidade, estado, cep,
                    anoAquisicao, mesAquisicao,
                    areaFormatada, carbonoFormatado,
                    geometriaWkt, geometriaGeoJson,
                    donoId, donoNome, donoEmail
            );
        }
    }
}