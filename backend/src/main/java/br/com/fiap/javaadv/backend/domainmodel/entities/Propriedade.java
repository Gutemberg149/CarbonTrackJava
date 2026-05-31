//package br.com.fiap.javaadv.backend.domainmodel.entities;
//
//import jakarta.persistence.*;
//import lombok.*;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//
//import java.awt.*;
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
//    @Column(columnDefinition = "SDO_GEOMETRY")
//    private Polygon geometria;
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
//    @Column(length = 8)
//    private String cep;
//
//    @Column(name = "area_hectares")
//    private Double areaHectares;
//
//    @Column(name = "carbono_estimado")
//    private Double carbonoEstimado;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "dono_id", nullable = false)
//    @JsonIgnore
//    private User dono;
//
//    /**
//     * Campo para geometria do polígono em formato WKT (Well-Known Text).
//     * Formato: POLYGON((-46.6333 -23.5505, -46.6333 -23.5605, ...))
//     */
//
//    /**
//     * SRID padrão para projetos brasileiros.
//     * 4326 = WGS84 (GPS - latitude/longitude)
//     * 4674 = SIRGAS 2000 (recomendado para o Brasil)
//     */
//    public static final int SRID = 4674;
//}
package br.com.fiap.javaadv.backend.domainmodel.entities;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

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

    /**
     * Campo para geometria do polígono (Oracle Spatial)
     * O mapeamento 'SDO_GEOMETRY' é o tipo nativo do Oracle Spatial.
     * O Hibernate Spatial fará a tradução automática entre o JTS Polygon
     * e o formato interno do Oracle (SDO_GEOMETRY).
     */
    @Column(columnDefinition = "SDO_GEOMETRY")
    private Polygon geometria;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dono_id", nullable = false)
    @JsonIgnore
    private User dono;

    /**
     * Versão em JSON das coordenadas para compatibilidade com APIs REST
     * e para facilitar a integração com front-end.
     */
    @Transient
    private String coordenadasJson;

    /**
     * SRID padrão para projetos brasileiros.
     * 4326 = WGS84 (GPS - latitude/longitude)
     * 4674 = SIRGAS 2000 (recomendado para o Brasil)
     */
    public static final int SRID = 4674;

    /**
     * GeometryFactory para criar geometrias com SRID correto
     */
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    /**
     * Método auxiliar para definir a geometria com SRID correto
     */
    public void setGeometriaComSrid(Polygon polygon) {
        if (polygon != null) {
            polygon.setSRID(SRID);
            this.geometria = polygon;
        }
    }

    /**
     * Método auxiliar para verificar se a geometria é válida
     */
    public boolean isGeometriaValida() {
        return geometria != null && !geometria.isEmpty();
    }

    /**
     * Método auxiliar para obter a área em hectares da geometria
     * Retorna null se a geometria for inválida
     */
    public Double getAreaCalculada() {
        if (!isGeometriaValida()) {
            return null;
        }
        // Área em metros quadrados, convertendo para hectares
        double areaM2 = geometria.getArea();
        return areaM2 / 10000.0;
    }

    /**
     * Método auxiliar para atualizar área e carbono estimado
     * baseado na geometria atual
     */
    public void atualizarPorGeometria() {
        if (isGeometriaValida()) {
            this.areaHectares = getAreaCalculada();
        }
    }

    /**
     * Método auxiliar para converter Polygon para WKT
     */
    public String getGeometriaWkt() {
        if (geometria == null) {
            return null;
        }
        return geometria.toText();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        atualizarPorGeometria();
    }
}