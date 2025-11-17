package br.com.helpTI.helpdeskapi.security;

import java.util.Collection;
import java.util.Set; // Importe este

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// Esta classe "traduz" nossos Clientes/Tecnicos para o formato UserDetails
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String senha;
    private Collection<? extends GrantedAuthority> authorities; // As "PERMISSÕES"

    public UserDetailsImpl(Long id, String email, String senha, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        // Transforma o Set<String> (ex: "ROLE_TECNICO") em algo que o Spring entende
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Long getId() {
        return this.id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email; // Nosso "username" é o e-mail
    }

    // --- Métodos de controle de conta (deixamos true por enquanto) ---
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}