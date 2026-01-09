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
                // Rota de pagamento (Admin)
                .requestMatchers(HttpMethod.POST, "/api/chamados/tecnico/*/pagar").hasAuthority("ROLE_ADMIN")
                
                // Rotas Públicas (Login, Empresas, Senha)
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/api/empresas").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reset-password").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/forgot-password").permitAll()

                // --- A CORREÇÃO MÁGICA ESTÁ AQUI ---
                // Libera o download de imagens para o navegador conseguir exibir
                .requestMatchers(HttpMethod.GET, "/api/files/**").permitAll()
                // -----------------------------------

                // Dashboards
                .requestMatchers("/api/dashboard/admin").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/dashboard/tecnico").hasAuthority("ROLE_TECNICO")
                
                // Chamados e Categorias (Autenticados)
                .requestMatchers("/api/chamados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")
                .requestMatchers("/api/categorias/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")
                
                // Swagger (Opcional, se usar)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                // Qualquer outra coisa exige login
                .anyRequest().authenticated()
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Permite o Frontend (localhost:5173 e 5175)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5175"));
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return source;
    }
}