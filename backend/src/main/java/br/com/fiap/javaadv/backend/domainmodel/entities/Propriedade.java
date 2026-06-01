//package br.com.fiap.javaadv.backend.domainmodel.entities;
//
//import jakarta.persistence.*;
//import lombok.*;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import java.util.UUID;
//
//@Entity
//@Table(name = "TB_PROPRIEDADE")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Propriedade {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(updatable = false, nullable = false)
//    private UUID id;
//
//    @Column(nullable = false, length = 100)
//    private String nome;
//
//    @Column(length = 500)
//    private String endereco;
//
//    @Column(length = 50)
//    private String cidade;
//
//    @Column(length = 2)
//    private String estado;
//
//    @Column(length = 10)
//    private String cep;
//
//    /**
//     * Área em hectares - CALCULADA AUTOMATICAMENTE a partir da geometria
//     * Não deve ser informada manualmente, apenas para leitura/consulta
//     */
//    @Column(name = "area_hectares")
//    private Double areaHectares;
//
//    /**
//     * Carbono estimado em toneladas (tCO₂) - CALCULADO AUTOMATICAMENTE
//     * Fórmula: Área (ha) × 150 t/ha × 0.47
//     * Não deve ser informado manualmente, apenas para leitura/consulta
//     */
//    @Column(name = "carbono_estimado")
//    private Double carbonoEstimado;
//
//    /**
//     * Dono/proprietário da propriedade
//     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "dono_id", nullable = false)
//    @JsonIgnore
//    private User dono;
//
//    /**
//     * Geometria da propriedade em formato WKT (Well-Known Text)
//     * Este é o campo FONTE DA VERDADE
//     * Exemplo: "POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, ...))"
//     */
//    @Column(name = "geometria", columnDefinition = "CLOB", nullable = false)
//    private String geometria;
//}

package br.com.fiap.javaadv.backend.domainmodel.entities;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.YearMonth;
import java.util.UUID;

@Entity
@Table(name = "TB_PROPRIEDADE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Propriedade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String endereco;

    @Column(length = 50)
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(length = 10)
    private String cep;

    @Column(name = "area_hectares")
    private Double areaHectares;

    @Column(name = "carbono_estimado")
    private Double carbonoEstimado;

    @Column(name = "ano_aquisicao")
    private Integer anoAquisicao;

    @Column(name = "mes_aquisicao")
    private Integer mesAquisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dono_id", nullable = false)
    @JsonIgnore
    private User dono;

    @Column(name = "geometria", columnDefinition = "CLOB", nullable = false)
    private String geometria;
}