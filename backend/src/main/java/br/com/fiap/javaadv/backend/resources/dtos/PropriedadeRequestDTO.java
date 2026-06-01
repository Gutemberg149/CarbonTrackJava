//package br.com.fiap.javaadv.backend.resources.dtos;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import io.swagger.v3.oas.annotations.media.Schema;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.UUID;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@Schema(description = "DTO de requisição da propriedade")
//public class PropriedadeRequestDTO {
//
//    @Schema(description = "Nome da propriedade", example = "Fazenda Boa Vista", required = true)
//    @NotBlank(message = "O nome da propriedade é obrigatório")
//    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
//    private String nome;
//
//    @Schema(description = "Endereço completo", example = "Rodovia BR-101, Km 200", required = true)
//    @NotBlank(message = "O endereço é obrigatório")
//    @Size(max = 500, message = "O endereço deve ter no máximo 500 caracteres")
//    private String endereco;
//
//    @Schema(description = "Cidade", example = "São Paulo")
//    private String cidade;
//
//    @Schema(description = "Estado (sigla)", example = "SP")
//    @Size(min = 2, max = 2, message = "Estado deve ser a sigla de 2 letras")
//    private String estado;
//
//    @Schema(description = "CEP", example = "01234-567")
//    @Size(min = 8, max = 9, message = "CEP deve ter 8 ou 9 caracteres")
//    private String cep;
//
//    // ❌ REMOVER areaHectares - será calculado automaticamente
//    // private Double areaHectares;
//
//    @Schema(description = "Coordenadas em formato JSON")
//    private String coordenadasJson;
//
//    @Schema(description = "Geometria em formato WKT", example = "POLYGON((-46.6333 -23.5505, ...))")
//    private String geometriaWkt;
//
//    @Schema(description = "ID do proprietário/dono", required = true)
//    @NotNull(message = "O ID do dono é obrigatório")
//    private UUID usuarioId;
//}

package br.com.fiap.javaadv.backend.resources.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO de requisição da propriedade")
public class PropriedadeRequestDTO {

    @Schema(description = "Nome da propriedade", example = "Fazenda Boa Vista", required = true)
    @NotBlank(message = "O nome da propriedade é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Schema(description = "Endereço completo", example = "Rodovia BR-101, Km 200")
    private String endereco;

    @Schema(description = "Cidade", example = "São Paulo")
    private String cidade;

    @Schema(description = "Estado (sigla)", example = "SP")
    @Size(min = 2, max = 2, message = "Estado deve ser a sigla de 2 letras")
    private String estado;

    @Schema(description = "CEP", example = "01234-567")
    @Size(min = 8, max = 9, message = "CEP deve ter 8 ou 9 caracteres")
    private String cep;

    @Schema(description = "Ano de aquisição", example = "2020")
    @Min(value = 1900, message = "Ano deve ser maior que 1900")
    @Max(value = 2100, message = "Ano deve ser menor que 2100")
    private Integer anoAquisicao;

    @Schema(description = "Mês de aquisição (1-12)", example = "5")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    private Integer mesAquisicao;

    @Schema(description = "Geometria em formato WKT", example = "POLYGON((-46.6333 -23.5505, ...))")
    private String geometriaWkt;

    @Schema(description = "Coordenadas em formato JSON (depreciado)")
    private String coordenadasJson;

    @Schema(description = "ID do proprietário/dono", required = true)
    @NotNull(message = "O ID do dono é obrigatório")
    private UUID usuarioId;
}