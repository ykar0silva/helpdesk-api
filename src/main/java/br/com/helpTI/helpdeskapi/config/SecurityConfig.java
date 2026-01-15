package br.com.helpTI.helpdeskapi.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.helpTI.helpdeskapi.security.SecurityFilter;
import br.com.helpTI.helpdeskapi.security.UserDetailsServiceIm;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceIm userDetailsService;

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // 1. Rotas Públicas (Login, Cadastro, Arquivos)
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/empresas").permitAll() // Cadastro de empresa
                .requestMatchers(HttpMethod.POST, "/api/clientes").permitAll() // auto-cadastro de cliente
                .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll() // Imagens
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers("/error").permitAll()

                // 2. Rotas Específicas (Ordem importa: Do mais específico para o genérico)
                
                // Pagamento (Só Admin)
                .requestMatchers(HttpMethod.POST, "/api/chamados/tecnico/*/pagar").hasAuthority("ROLE_ADMIN")
                
                // Dashboards Específicos
                .requestMatchers("/api/dashboard/admin").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/dashboard/tecnico").hasAuthority("ROLE_TECNICO")
                .requestMatchers("/api/dashboard/cliente").hasAuthority("ROLE_CLIENTE")

                // 3. Chamados 
                // Permitimos explicitamente GET para todos os perfis logados
                .requestMatchers(HttpMethod.GET, "/api/chamados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")
                .requestMatchers(HttpMethod.POST, "/api/chamados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")
                .requestMatchers(HttpMethod.PUT, "/api/chamados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")

                // Categorias
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")

                // Swagger (Documentação)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Qualquer outra rota precisa estar autenticado
                .anyRequest().authenticated()
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Garante que o frontend consiga acessar
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5175"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers essenciais para o token trafegar
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }
}