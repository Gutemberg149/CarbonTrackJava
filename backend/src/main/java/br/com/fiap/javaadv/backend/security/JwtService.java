package br.com.fiap.javaadv.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Refatorado para garantir que o Spring resolva o placeholder com fallback
    @Value("${security.jwt.secret:chave-super-secreta-para-jwt-2026-carbon-track}")
    private String secret;

    @Value("${security.jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSigningKey() {
        // Garantimos que a chave tenha o tamanho mínimo exigido pelo HMAC-SHA (256 bits/32 bytes)
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String nome) {
        return Jwts.builder()
                .subject(email)
                .claim("nome", nome)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, String email) {
        return (extractEmail(token).equals(email) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}