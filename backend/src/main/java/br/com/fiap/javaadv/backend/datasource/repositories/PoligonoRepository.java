package br.com.fiap.javaadv.backend.datasource.repositories;


import br.com.fiap.javaadv.backend.domainmodel.entities.Poligono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PoligonoRepository extends JpaRepository<Poligono, UUID> {

    List<Poligono> findByPropriedadeId(UUID propriedadeId);

    boolean existsByPropriedadeId(UUID propriedadeId);
}
