package br.com.helpTI.helpdeskapi.security;

import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // 1. Marca como um componente gerenciado pelo Spring
public class SecurityFilter extends OncePerRequestFilter { // 2. Roda UMA VEZ por requisição

    @Autowired
    private JwtTokenService jwtTokenService; // Usamos este só para pegar o segredo (melhorar depois)

    @Value("${api.security.token.secret}") // 3. Pega a mesma chave secreta
    private String secretKey;

    // 4. Precisamos dos repositórios para recarregar o usuário
    @Autowired
    private ClienteRepository clienteRepo;
    @Autowired
    private TecnicoRepository tecnicoRepo;
    @Autowired
    private EmpresaRepository empresaRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 5. Pega o token da requisição
        var token = this.recoverToken(request);

        if(token != null) {
            // 6. Se o token veio, vamos validá-lo
            String email = this.validateToken(token); // Pega o e-mail (subject) de dentro do token

            // 7. Carrega o usuário do banco usando o e-mail
            UserDetails userDetails = loadUserByEmail(email);

            if (userDetails != null) {
                // 8. Se o usuário existe, autentica ele "na mão" para o Spring
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // 9. Salva a autenticação no contexto do Spring
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 10. (Importante) Manda a requisição continuar seu caminho (para o Controller)
        filterChain.doFilter(request, response);
    }

    // Método para extrair o token do Header "Authorization"
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        // O token vem como "Bearer eyJ..."
        return authHeader.replace("Bearer ", ""); 
    }

    // Método para validar o token e extrair o "subject" (e-mail)
    private String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.require(algorithm)
                .withIssuer("HelpTI-API")
                .build()
                .verify(token) // Se o token for inválido, aqui dá exceção
                .getSubject(); // Retorna o e-mail
        } catch (JWTVerificationException exception) {
            return ""; // Se inválido, retorna vazio
        }
    }

    // Método duplicado (poderíamos otimizar), mas que busca o UserDetails
    private UserDetails loadUserByEmail(String email) {
        // (Este código é o mesmo do UserDetailsServiceIm)
        var clienteOpt = clienteRepo.findByEmail(email);
        if(clienteOpt.isPresent()) {
            var cliente = clienteOpt.get();
            var roles = Set.of("ROLE_CLIENTE", "ROLE_" + cliente.getPerfil().toUpperCase());
            return new UserDetailsImpl(cliente.getId(), cliente.getEmail(), cliente.getSenha(), roles);
        }

        var tecnicoOpt = tecnicoRepo.findByEmail(email);
        if(tecnicoOpt.isPresent()) {
            var tecnico = tecnicoOpt.get();
            var roles = Set.of("ROLE_TECNICO");
            return new UserDetailsImpl(tecnico.getId(), tecnico.getEmail(), tecnico.getSenha(), roles);
        }

        var empresaOpt = empresaRepo.findByEmailResponsavel(email);
        if(empresaOpt.isPresent()) {
            var empresa = empresaOpt.get();
            var roles = Set.of("ROLE_ADMIN");
            return new UserDetailsImpl(empresa.getId(), empresa.getEmailResponsavel(), empresa.getSenha(), roles);
        }
        return null;
    }
}