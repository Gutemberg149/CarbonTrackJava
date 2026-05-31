package br.com.fiap.javaadv.backend.datasource.repositories;

import br.com.fiap.javaadv.backend.domainmodel.entities.CreditoCarbono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface CreditoCarbonoRepository extends JpaRepository<CreditoCarbono, UUID> {
    List<CreditoCarbono> findByPropriedadeId(UUID propriedadeId);
}
