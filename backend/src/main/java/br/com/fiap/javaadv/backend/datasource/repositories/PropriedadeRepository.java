package br.com.fiap.javaadv.backend.datasource.repositories;

import br.com.fiap.javaadv.backend.domainmodel.entities.Propriedade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface PropriedadeRepository extends JpaRepository<Propriedade, UUID> {

}
