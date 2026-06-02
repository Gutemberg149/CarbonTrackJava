package br.com.fiap.javaadv.backend.services;

import br.com.fiap.javaadv.backend.datasource.repositories.UserRepository;
import br.com.fiap.javaadv.backend.resources.dtos.AuthResponseDTO;
import br.com.fiap.javaadv.backend.resources.dtos.LoginRequestDTO;
import br.com.fiap.javaadv.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("🔐 Tentativa de login: {}", loginRequest.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.senha()
                )
        );

        var user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = jwtService.generateToken(user.getEmail(), user.getNome());

        log.info("✅ Login realizado com sucesso: {}", user.getEmail());

        return new AuthResponseDTO(token, "Bearer", user.getEmail(), user.getNome());
    }
}