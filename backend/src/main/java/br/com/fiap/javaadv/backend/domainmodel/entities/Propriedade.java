package br.com.fiap.javaadv.backend.domainmodel.entities;

import br.com.fiap.javaadv.backend.domainmodel.embeddables.Endereco;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Embedded
    private Endereco endereco;

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