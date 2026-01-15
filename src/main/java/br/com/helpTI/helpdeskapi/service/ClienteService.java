package br.com.helpTI.helpdeskapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;
    
    @Autowired
    private EmpresaRepository empresaRepository;
    
    @Autowired 
    private PasswordEncoder passwordEncoder;

    public Cliente findById(Long id) {
        Optional<Cliente> obj = repository.findById(id);
        return obj.orElse(null);
    }

    public List<Cliente> findAll() {
        // Lógica de Segurança: Gestor só vê seus clientes
        String emailUsuario = getEmailUsuarioLogado();
        Optional<Cliente> usuarioLogado = repository.findByEmailIgnoreCase(emailUsuario);

        if (usuarioLogado.isPresent()) {
            Cliente gestor = usuarioLogado.get();
            // Se tiver empresa vinculada, filtra por ela
            if (gestor.getEmpresa() != null) {
                return repository.findAllByEmpresaId(gestor.getEmpresa().getId());
            }
        }
        
        // Fallback: Se for admin técnico ou algo assim, retorna tudo
        return repository.findAll();
    }

    public Cliente create(Cliente obj) {
        obj.setId(null);
        obj.setSenha(passwordEncoder.encode(obj.getSenha()));
        
        // =====================================================================
        // 🚨 CORREÇÃO DA HIERARQUIA E DO ERRO SQL 🚨
        // =====================================================================
        
        // 1. Descobre quem está criando o cliente (O Gestor Logado)
        String emailGestor = getEmailUsuarioLogado();
        Cliente gestor = repository.findByEmailIgnoreCase(emailGestor)
                .orElseThrow(() -> new RuntimeException("Erro: Usuário logado não encontrado no banco."));

        // 2. Pega a empresa desse Gestor (Ex: TechSolutions)
        Empresa empresaDoGestor = gestor.getEmpresa();
        
        if (empresaDoGestor == null) {
            // Caso de borda: Se um admin técnico sem empresa criar, vincula à Matriz (ID 1)
            empresaDoGestor = empresaRepository.findById(1L).orElse(null);
        }

        // 3. O PULO DO GATO: Vincula o novo cliente à empresa do Gestor
        obj.setEmpresa(empresaDoGestor);
        
        // 4. Preenche a coluna visual (Resolvendo o erro Not Null)
        if (empresaDoGestor != null) {
            obj.setEmpresaDoCliente(empresaDoGestor.getNomeFantasia());
        } else {
            obj.setEmpresaDoCliente("HelpTI Matriz"); // Fallback final
        }

        // =====================================================================

        return repository.save(obj);
    }

    public Cliente update(Long id, Cliente obj) {
        Cliente oldObj = findById(id);
        if (oldObj != null) {
            oldObj.setNome(obj.getNome());
            oldObj.setEmail(obj.getEmail());
            oldObj.setPerfil(obj.getPerfil());
            
            // Nota: Geralmente não mudamos a empresa num update simples, 
            // mas se precisar, tem que passar o objeto Empresa correto.
            // Por segurança, mantemos a empresa original se o update não trouxer nova.
            if (obj.getEmpresaDoCliente() != null) {
                 oldObj.setEmpresaDoCliente(obj.getEmpresaDoCliente());
            }
            
            if(obj.getSenha() != null && !obj.getSenha().isEmpty()) {
                 oldObj.setSenha(passwordEncoder.encode(obj.getSenha()));
            }
            return repository.save(oldObj);
        }
        return null;
    }
    
    // Método auxiliar para pegar o email do token
    private String getEmailUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}