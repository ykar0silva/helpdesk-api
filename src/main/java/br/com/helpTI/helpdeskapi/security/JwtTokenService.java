package br.com.helpTI.helpdeskapi.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
@Service
public class JwtTokenService {

    // 1. Vamos buscar esta "chave secreta" do application.properties
    @Value("${api.security.token.secret}")
    private String secretKey;

    // 2. Método para GERAR o token
    public String generateToken(UserDetailsImpl userDetails) {
        try {
            // 3. Define o "algoritmo" de encriptação (HMAC256) com a nossa chave
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String token = JWT.create()
                .withIssuer("HelpTI-API") 
                .withSubject(userDetails.getUsername()) 
                .withExpiresAt(getExpirationDate()) 
                .withClaim("roles", roles)
                .withClaim("id", userDetails.getId())
                .withClaim("empresaId", userDetails.getEmpresaId())
                .sign(algorithm);
            return token;

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar o token JWT", exception);
        }
    }

    // 4. Define o tempo de expiração (ex: 2 horas a partir de agora)
    private Instant getExpirationDate() {
        return LocalDateTime.now()
            .plusHours(2)
            .toInstant(ZoneOffset.of("-03:00")); // Fuso de Palmas (UTC-3)
    }
    
}