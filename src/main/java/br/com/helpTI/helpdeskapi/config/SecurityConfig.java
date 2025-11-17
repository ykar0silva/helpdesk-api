package br.com.helpTI.helpdeskapi.config;

import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration // 1. Diz ao Spring que esta é uma classe de configuração
@EnableWebSecurity // 2. Liga a "tomada" da segurança web do Spring
public class SecurityConfig {

	@Autowired // 1. INJETA O NOSSO SERVIÇO DE BUSCA DE UTILIZADOR
    private UserDetailsServiceIm userDetailsService;

	@Autowired // 1. INJETE O NOSSO FILTRO
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
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder()); // Ensina-o a usar o BCrypt
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
            	.requestMatchers("/api/login").permitAll()
            	.requestMatchers("/api/empresas").permitAll()
            	.anyRequest().authenticated() 
                    )
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}