//package br.com.fiap.javaadv.backend.domainmodel.entities;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.util.Objects;
//import java.util.UUID;
//
//@Entity
//@Table(name = "TB_POLIGONOS_V2")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@ToString(exclude = "propriedade")
//public class Poligono {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "id", updatable = false, nullable = false)
//    private UUID id;
//
//    @Column(name = "geometria", columnDefinition = "CLOB")
//    private String geometria;
//
//    @Column(name = "area_hectares")
//    private Double areaHectares;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "propriedade_id", nullable = false, unique = true)
//    private Propriedade propriedade;
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof Poligono that)) return false;
//        return Objects.equals(id, that.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id);
//    }
//}
package br.com.fiap.javaadv.backend.domainmodel.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TB_POLIGONOS_V2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "propriedade")
public class Poligono {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "geometria", columnDefinition = "CLOB")
    private String geometria;

    @Column(name = "area_hectares", columnDefinition = "NUMBER(15,2)")
    private Double areaHectares;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propriedade_id", nullable = false, unique = true)
    private Propriedade propriedade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Poligono that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}