package br.com.helpTI.helpdeskapi.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String senha;
    private Long empresaId; // Novo campo para armazenar o ID da empresa
    
    // Mudamos para Collection para ser mais genérico (aceita Set e List)
    private Collection<? extends GrantedAuthority> authorities; 

    // --- CONSTRUTOR 1: O NOVO (Que resolve seu erro) ---
    // Aceita já a lista de permissões prontas que vêm do UserDetailsServiceIm
    public UserDetailsImpl(Long id, String email, String senha, Collection<? extends GrantedAuthority> authorities, Long empresaId) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.authorities = authorities;
        this.empresaId = empresaId;
    }

    // --- CONSTRUTOR 2: O ANTIGO (Para compatibilidade) ---
    // Mantemos este caso alguma outra parte do código ainda envie Strings
    public UserDetailsImpl(Long id, String email, String senha, Set<String> roles, Long empresaId) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        this.empresaId = empresaId;
    }

    public Long getId() {
        return this.id;
    }

    public Long getEmpresaId() {
        return this.empresaId;
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
        return this.email;
    }

    // --- Controles de Conta (Padrão: tudo true) ---
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}