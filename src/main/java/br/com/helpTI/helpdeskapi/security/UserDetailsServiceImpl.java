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
import br.com.helpTI.helpdeskapi.domain.enums.TipoEmpresa; 
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClienteRepository clienteRepo;
    
    @Autowired
    private TecnicoRepository tecnicoRepo;
    
    @Autowired
    private EmpresaRepository empresaRepo; 

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // =====================================================================
        // 1. LOGIN INSTITUCIONAL (EMPRESAS: MATRIZ OU PROVEDOR)
        // =====================================================================
        // Quem loga aqui é a "CNPJ" (admin@helpti.com ou provedor@tech.com)
        Optional<Empresa> empresaOpt = empresaRepo.findByEmailResponsavel(email);
        if(empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            Set<GrantedAuthority> authorities = new HashSet<>();
            
            if (empresa.getTipoEmpresa() == TipoEmpresa.MATRIZ) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } 
            else if (empresa.getTipoEmpresa() == TipoEmpresa.PRESTADORA) {
                // Aqui entra o provedor@tech.com
                authorities.add(new SimpleGrantedAuthority("ROLE_PRESTADORA"));
            } 
            else {
                // Caso raro, mas seguro tratar como Gestor
                authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            }
            
            return new UserDetailsImpl(empresa.getId(), empresa.getEmailResponsavel(), empresa.getSenha(), authorities);
        }

        // =====================================================================
        // 2. LOGIN DE TÉCNICOS
        // =====================================================================
        Optional<Tecnico> tecnicoOpt = tecnicoRepo.findByEmailIgnoreCase(email);
        if(tecnicoOpt.isPresent()) {
            Tecnico tecnico = tecnicoOpt.get();
            Set<GrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_TECNICO"));
            return new UserDetailsImpl(tecnico.getId(), tecnico.getEmail(), tecnico.getSenha(), authorities);
        }

        // =====================================================================
        // 3. LOGIN PESSOAL (CLIENTES / GESTORES / FUNCIONÁRIOS)
        // =====================================================================
        // Quem loga aqui é o CPF (gestor@tech.com ou dono@padaria.com)
        Optional<Cliente> clienteOpt = clienteRepo.findByEmailIgnoreCase(email);
        if(clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            Set<GrantedAuthority> authorities = new HashSet<>();
            
            // Lógica Simplificada: O Perfil define a Role.
            // Não importa se a empresa dele é Provedora ou Cliente Final.
            
            if ("GESTOR".equalsIgnoreCase(cliente.getPerfil())) {
                authorities.add(new SimpleGrantedAuthority("ROLE_GESTOR"));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            }
            
            return new UserDetailsImpl(cliente.getId(), cliente.getEmail(), cliente.getSenha(), authorities);
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }
}