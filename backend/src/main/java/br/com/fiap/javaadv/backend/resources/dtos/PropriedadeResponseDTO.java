package br.com.fiap.javaadv.backend.resources.dtos;

import br.com.fiap.javaadv.backend.domainmodel.embeddables.Endereco;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import br.com.fiap.javaadv.backend.config.TwoDecimalSerializer;

import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO de resposta da propriedade")
public class PropriedadeResponseDTO extends BaseResponseDTO {

    @Schema(description = "ID único da propriedade")
    private final UUID id;

    @Schema(description = "Nome da propriedade")
    private final String nome;

    @JsonUnwrapped
    private final Endereco endereco;

    @Schema(description = "Ano de aquisição")
    private final Integer anoAquisicao;

    @Schema(description = "Mês de aquisição")
    private final Integer mesAquisicao;

    @Schema(description = "Área em hectares (calculada automaticamente)")
    @JsonSerialize(using = TwoDecimalSerializer.class)
    private final Double areaHectares;

    @Schema(description = "Carbono sequestrado estimado em toneladas (tCO₂)")
    @JsonSerialize(using = TwoDecimalSerializer.class)
    private final Double carbonoEstimado;

    @Schema(description = "Geometria em formato WKT")
    private final String geometriaWkt;

    @Schema(description = "Geometria em formato GeoJSON")
    private final Map<String, Object> geometriaGeoJson;

    @Schema(description = "ID do proprietário")
    private final UUID donoId;

    @Schema(description = "Nome do proprietário")
    private final String donoNome;

    @Schema(description = "Email do proprietário")
    private final String donoEmail;

    // Construtor
    public PropriedadeResponseDTO(UUID id, String nome, Endereco endereco,
                                  Integer anoAquisicao, Integer mesAquisicao,
                                  Double areaHectares, Double carbonoEstimado,
                                  String geometriaWkt, Map<String, Object> geometriaGeoJson,
                                  UUID donoId, String donoNome, String donoEmail) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.anoAquisicao = anoAquisicao;
        this.mesAquisicao = mesAquisicao;
        this.areaHectares = areaHectares;
        this.carbonoEstimado = carbonoEstimado;
        this.geometriaWkt = geometriaWkt;
        this.geometriaGeoJson = geometriaGeoJson;
        this.donoId = donoId;
        this.donoNome = donoNome;
        this.donoEmail = donoEmail;
    }

    // Getters
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public Endereco getEndereco() { return endereco; }
    public Integer getAnoAquisicao() { return anoAquisicao; }
    public Integer getMesAquisicao() { return mesAquisicao; }
    public Double getAreaHectares() { return areaHectares; }
    public Double getCarbonoEstimado() { return carbonoEstimado; }
    public String getGeometriaWkt() { return geometriaWkt; }
    public Map<String, Object> getGeometriaGeoJson() { return geometriaGeoJson; }
    public UUID getDonoId() { return donoId; }
    public String getDonoNome() { return donoNome; }
    public String getDonoEmail() { return donoEmail; }

    // Getters específicos para compatibilidade com JSON
    public String getEnderecoCompleto() { return endereco != null ? endereco.getEndereco() : null; }
    public String getCidade() { return endereco != null ? endereco.getCidade() : null; }
    public String getEstado() { return endereco != null ? endereco.getEstado() : null; }
    public String getCep() { return endereco != null ? endereco.getCep() : null; }

    // Builder
    public static PropriedadeResponseDTOBuilder builder() {
        return new PropriedadeResponseDTOBuilder();
    }

    public static class PropriedadeResponseDTOBuilder {
        private UUID id;
        private String nome;
        private Endereco endereco;
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
        public PropriedadeResponseDTOBuilder endereco(Endereco endereco) { this.endereco = endereco; return this; }
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
                    id, nome, endereco, anoAquisicao, mesAquisicao,
                    areaFormatada, carbonoFormatado,
                    geometriaWkt, geometriaGeoJson,
                    donoId, donoNome, donoEmail
            );
        }
    }
}