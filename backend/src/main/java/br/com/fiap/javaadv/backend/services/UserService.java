package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.UserRepository;
import br.com.fiap.javaadv.backend.domainmodel.entities.User;
import br.com.fiap.javaadv.backend.resources.dtos.UserRequestDTO;
import br.com.fiap.javaadv.backend.resources.dtos.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    @Transactional
    public UserResponseDTO salvar(UserRequestDTO dto) {
        // Utiliza o Builder da entidade User para evitar problemas de construtores
        User user = User.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(dto.senha())
                .build();

        return toResponseDTO(repository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .toList(); // Sintaxe moderna Java 16+ para substituir .collect(Collectors.toList())
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
    }

    // Método auxiliar de conversão mantido privado e clean
    private UserResponseDTO toResponseDTO(User u) {
        return new UserResponseDTO(u.getId(), u.getNome(), u.getEmail());
    }
}