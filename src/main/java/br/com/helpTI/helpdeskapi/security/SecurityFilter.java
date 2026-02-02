package br.com.helpTI.helpdeskapi.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Value("${api.security.token.secret}")
    private String secretKey;

    @Autowired
    private ClienteRepository clienteRepo;
    
    @Autowired
    private TecnicoRepository tecnicoRepo;
    
    @Autowired
    private EmpresaRepository empresaRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var token = this.recoverToken(request);

        if(token != null) {
            String email = this.validateToken(token);
            
            // Carrega o usuário
            UserDetails userDetails = loadUserByEmail(email);

            if (userDetails != null) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", ""); 
    }

    private String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.require(algorithm)
                .withIssuer("HelpTI-API")
                .build()
                .verify(token)
                .getSubject();
        } catch (JWTVerificationException exception) {
            return ""; 
        }
    }

    private UserDetails loadUserByEmail(String email) {
        // =====================================================================
        // 1. CLIENTE
        // =====================================================================
        var clienteOpt = clienteRepo.findByEmailIgnoreCase(email);
        if(clienteOpt.isPresent()) {
            var cliente = clienteOpt.get();
            
            // CORREÇÃO: Usar HashSet para evitar erro de duplicata e SimpleGrantedAuthority para compatibilidade
            Set<GrantedAuthority> authorities = new HashSet<>();
            
            // Regra base
            authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            
            // Regra do perfil (Gestor, etc)
            if (cliente.getPerfil() != null) {
                String perfilUpper = cliente.getPerfil().toUpperCase();
                String role = perfilUpper.startsWith("ROLE_") ? perfilUpper : "ROLE_" + perfilUpper;
                authorities.add(new SimpleGrantedAuthority(role));
            }

            Long empresaId = cliente.getEmpresa() != null ? cliente.getEmpresa().getId() : null;
            return new UserDetailsImpl(cliente.getId(), cliente.getEmail(), cliente.getSenha(), authorities, empresaId);
        }

        // =====================================================================
        // 2. TÉCNICO
        // =====================================================================
        var tecnicoOpt = tecnicoRepo.findByEmailIgnoreCase(email);
        if(tecnicoOpt.isPresent()) {
            var tecnico = tecnicoOpt.get();
            
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_TECNICO"));
            
            Long empresaId = tecnico.getEmpresa() != null ? tecnico.getEmpresa().getId() : null;
            return new UserDetailsImpl(tecnico.getId(), tecnico.getEmail(), tecnico.getSenha(), authorities, empresaId);
        }

        // =====================================================================
        // 3. EMPRESA
        // =====================================================================
        var empresaOpt = empresaRepo.findByEmailResponsavel(email);
        if(empresaOpt.isPresent()) {
            var empresa = empresaOpt.get();
            
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            return new UserDetailsImpl(empresa.getId(), empresa.getEmailResponsavel(), empresa.getSenha(), authorities, empresa.getId());
        }
        
        return null;
    }
}