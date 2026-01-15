package br.com.helpTI.helpdeskapi.security;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;

@Service
public class UserDetailsServiceIm implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepo;
    
    @Autowired
    private TecnicoRepository tecnicoRepo;
    
    @Autowired
    private EmpresaRepository empresaRepo; 

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // =====================================================================
        // 1. CLIENTE
        // =====================================================================
        Optional<Cliente> clienteOpt = clienteRepo.findByEmailIgnoreCase(email);
        if(clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            // Correção do erro 500: Usamos HashSet para evitar duplicação
            Set<GrantedAuthority> authorities = new HashSet<>();
            
            // Adiciona papel padrão
            authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            
            // Adiciona perfil dinâmico (ex: GESTOR) se existir
            if (cliente.getPerfil() != null) {
                String perfil = cliente.getPerfil().toUpperCase();
                // Garante o prefixo ROLE_
                String role = perfil.startsWith("ROLE_") ? perfil : "ROLE_" + perfil;
                authorities.add(new SimpleGrantedAuthority(role));
            }
            
            return new UserDetailsImpl(cliente.getId(), cliente.getEmail(), cliente.getSenha(), authorities);
        }
        
        // =====================================================================
        // 2. TÉCNICO
        // =====================================================================
        Optional<Tecnico> tecnicoOpt = tecnicoRepo.findByEmailIgnoreCase(email);
        if(tecnicoOpt.isPresent()) {
            Tecnico tecnico = tecnicoOpt.get();
            Set<GrantedAuthority> authorities = new HashSet<>();
            
            authorities.add(new SimpleGrantedAuthority("ROLE_TECNICO"));
            
            // Se técnico tiver perfis extras, adicione aqui...
            
            return new UserDetailsImpl(tecnico.getId(), tecnico.getEmail(), tecnico.getSenha(), authorities);
        }

        // =====================================================================
        // 3. EMPRESA (DONO/MATRIZ/PRESTADORA)
        // =====================================================================
        Optional<Empresa> empresaOpt = empresaRepo.findByEmailResponsavel(email);
        if(empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            Set<GrantedAuthority> authorities = new HashSet<>();
            
            // Define como ADMIN ou PRESTADORA dependendo da lógica, aqui mantive ADMIN
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            
            return new UserDetailsImpl(empresa.getId(), empresa.getEmailResponsavel(), empresa.getSenha(), authorities);
        }

        throw new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email);
    }
}