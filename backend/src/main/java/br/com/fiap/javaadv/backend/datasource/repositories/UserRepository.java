package br.com.fiap.javaadv.backend.datasource.repositories;

import br.com.fiap.javaadv.backend.domainmodel.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Busca por e-mail para autenticação
    Optional<User> findByEmail(String email);
}
