package br.com.helpTI.helpdeskapi.security;

import java.util.Optional;
import java.util.Set; // Importe este

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
    private EmpresaRepository empresaRepo; // Para o login do "Dono/Admin"

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // 1. Procura no repositório de Clientes
        Optional<Cliente> clienteOpt = clienteRepo.findByEmail(email);
        if(clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            // Passa as permissões (ROLE_CLIENTE e o Perfil GESTOR/USUARIO)
            Set<String> roles = Set.of("ROLE_CLIENTE", "ROLE_" + cliente.getPerfil().toUpperCase());
            return new UserDetailsImpl(cliente.getId(), cliente.getEmail(), cliente.getSenha(), roles);
        }
        
        // 2. Se não achou, procura no repositório de Técnicos
        Optional<Tecnico> tecnicoOpt = tecnicoRepo.findByEmail(email);
        if(tecnicoOpt.isPresent()) {
            Tecnico tecnico = tecnicoOpt.get();
            Set<String> roles = Set.of("ROLE_TECNICO");
            return new UserDetailsImpl(tecnico.getId(), tecnico.getEmail(), tecnico.getSenha(), roles);
        }

        // 3. Se não achou, procura no repositório de Empresas (o "Dono")
        Optional<Empresa> empresaOpt = empresaRepo.findByEmailResponsavel(email);
        if(empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            Set<String> roles = Set.of("ROLE_ADMIN");
            // Usamos a senha do Dono (que precisaremos adicionar na entidade Empresa)
            return new UserDetailsImpl(empresa.getId(), empresa.getEmailResponsavel(), empresa.getSenha(), roles);
            
            // POR ENQUANTO, vamos parar aqui até ajustar a entidade Empresa
        }

        // 4. Se não achou em lugar nenhum
        throw new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email);
    }
}