package br.com.helpTI.helpdeskapi.security;

import java.util.Optional;
import java.util.Set; 

import org.springframework.beans.factory.annotation.Autowired;
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
        
        // 1. Cliente: Busca ignorando maiúsculas/minúsculas
        Optional<Cliente> clienteOpt = clienteRepo.findByEmailIgnoreCase(email);
        if(clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            Set<String> roles = Set.of("ROLE_CLIENTE", "ROLE_" + cliente.getPerfil().toUpperCase());
            return new UserDetailsImpl(cliente.getId(), cliente.getEmail(), cliente.getSenha(), roles);
        }
        
        // 2. Técnico: Busca ignorando maiúsculas/minúsculas
        Optional<Tecnico> tecnicoOpt = tecnicoRepo.findByEmailIgnoreCase(email);
        if(tecnicoOpt.isPresent()) {
            Tecnico tecnico = tecnicoOpt.get();
            Set<String> roles = Set.of("ROLE_TECNICO");
            return new UserDetailsImpl(tecnico.getId(), tecnico.getEmail(), tecnico.getSenha(), roles);
        }

        // 3. Empresa: Mantido o padrão antigo (Responsavel)
        Optional<Empresa> empresaOpt = empresaRepo.findByEmailResponsavel(email);
        if(empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            Set<String> roles = Set.of("ROLE_ADMIN");
            return new UserDetailsImpl(empresa.getId(), empresa.getEmailResponsavel(), empresa.getSenha(), roles);
        }

        throw new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email);
    }
}