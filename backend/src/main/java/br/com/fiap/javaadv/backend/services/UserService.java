package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.UserRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.User;
import br.com.fiap.javaadv.backend.resources.dtos.UserRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder; // ADICIONAR ESTE

    @Transactional
    public UserResponseDTO salvar(UserRequestDTO dto) {
        log.info("Salvando usuário: {}", dto.email());

        // Verificar se email já existe
        if (repository.existsByEmail(dto.email())) {
            throw new RuntimeException("Email já cadastrado: " + dto.email());
        }

        // Codificar a senha antes de salvar
        User user = User.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha())) // CODIFICAR A SENHA
                .build();

        User saved = repository.save(user);
        log.info("Usuário salvo com ID: {}", saved.getId());

        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO buscarPorId(UUID id) {
        return repository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + id));
    }

    @Transactional
    public UserResponseDTO atualizar(UUID id, UserRequestDTO dto) {
        return repository.findById(id)
                .map(existente -> {
                    existente.setNome(dto.nome());
                    existente.setEmail(dto.email());
                    // Só atualiza a senha se foi fornecida
                    if (dto.senha() != null && !dto.senha().isEmpty()) {
                        existente.setSenha(passwordEncoder.encode(dto.senha()));
                    }
                    return toResponseDTO(repository.save(existente));
                })
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para atualização"));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado com ID: " + id);
        }
        repository.deleteById(id);
        log.info("Usuário deletado: {}", id);
    }

    private UserResponseDTO toResponseDTO(User u) {
        return new UserResponseDTO(u.getId(), u.getNome(), u.getEmail());
    }
}