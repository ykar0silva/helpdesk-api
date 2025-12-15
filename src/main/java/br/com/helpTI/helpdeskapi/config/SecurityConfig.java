package br.com.helpTI.helpdeskapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import br.com.helpTI.helpdeskapi.security.SecurityFilter;
import br.com.helpTI.helpdeskapi.security.UserDetailsServiceIm;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // <-- IMPORTE
import org.springframework.web.cors.CorsConfigurationSource; // <-- IMPORTE
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // <-- IMPORTE
import java.util.List; // <-- IMPORTE
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration 
@EnableWebSecurity 
public class SecurityConfig {

	@Autowired 
    private UserDetailsServiceIm userDetailsService;

	@Autowired 
    private SecurityFilter securityFilter;
    // 3. Este é o "Bean" que sabe criptografar senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
 // 3. DIZ AO SPRING COMO ENCONTRAR OS UTILIZADORES
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userDetailsService)
//            .passwordEncoder(passwordEncoder()); // Ensina-o a usar o BCrypt
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(withDefaults()) 
            .csrf(csrf -> csrf.disable()) 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
            	.requestMatchers(HttpMethod.POST, "/api/chamados/tecnico/*/pagar").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/forgot-password").permitAll()
                .requestMatchers("/api/empresas").permitAll()

                
             // --- CORREÇÃO AQUI: TROCAR 'hasRole' por 'hasAuthority' ---
                .requestMatchers("/api/dashboard/admin").hasAuthority("ROLE_ADMIN") 
                .requestMatchers("/api/dashboard/tecnico").hasAuthority("ROLE_TECNICO")
                .requestMatchers("/api/chamados/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")
                .requestMatchers("/api/categorias/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_TECNICO", "ROLE_CLIENTE")
                .anyRequest().authenticated() 
            )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 3. DIZ QUAL ORIGEM É PERMITIDA 
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // 4. QUAIS MÉTODOS SÃO PERMITIDOS
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // 5. QUAIS HEADERS SÃO PERMITIDOS 
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        
        // 6. PERMITE CREDENCIAIS 
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5175"));
        
        return source;
    }
}