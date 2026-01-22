package br.com.helpTI.helpdeskapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;

@Service
public class TecnicoService {

    @Autowired
    private TecnicoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository; 

    @Autowired 
    private PasswordEncoder passwordEncoder;
    
    public Tecnico findById(Long id) {
        Optional<Tecnico> obj = repository.findById(id);
        return obj.orElse(null);
    }

    public List<Tecnico> findAll() {
        return repository.findAll();
    }

    // -------------------------------------------------------------------------
    // CORREÇÃO: Mudamos de 'TecnicoDashboardDTO' para 'Tecnico' (Entidade)
    // -------------------------------------------------------------------------
    @Transactional
    public Tecnico create(Tecnico obj) { // <--- Recebe a Entidade direto
        obj.setId(null); // Agora funciona, pois a entidade tem setId
        
        // Codifica a senha antes de salvar
        if (obj.getSenha() != null) {
            obj.setSenha(passwordEncoder.encode(obj.getSenha()));
        }

        // =====================================================================
        // 🔒 LÓGICA DE ISOLAMENTO DE EMPRESA
        // =====================================================================
        
        // 1. Descobre quem está logado
        String emailUsuarioLogado = getEmailUsuarioLogado();
        
        // 2. Tenta achar esse usuário como Cliente (Gestor)
        Optional<Cliente> gestorOpt = clienteRepository.findByEmailIgnoreCase(emailUsuarioLogado);
        
        if (gestorOpt.isPresent()) {
            // Se quem está criando é um Gestor, o Técnico DEVE ser da empresa dele
            Cliente gestor = gestorOpt.get();
            if (gestor.getEmpresa() != null) {
                obj.setEmpresa(gestor.getEmpresa()); // Força a empresa do Gestor
            } else {
                 throw new IllegalStateException("Erro: Gestor sem empresa vinculada tentou criar técnico.");
            }
        } else {
            // 3. Se não achou como Cliente, vê se é um Admin/Técnico da Matriz criando
            Optional<Tecnico> adminOpt = repository.findByEmail(emailUsuarioLogado);
            if (adminOpt.isPresent()) {
                // Se for Admin (Técnico), vincula à empresa dele (Matriz ou Filial)
                // Se o objeto vindo do front já tiver empresa (caso de Admin Matriz setando), respeita.
                // Caso contrário, vincula à do Admin.
                if (obj.getEmpresa() == null) {
                     obj.setEmpresa(adminOpt.get().getEmpresa());
                }
            }
        }
        // =====================================================================

        return repository.save(obj);
    }

    public List<Tecnico> findAllAtivos() {
        return repository.findByStatus("ATIVO");
    }
    
    public Tecnico update(Long id, Tecnico obj) {
        Tecnico oldObj = findById(id);
        
        if (oldObj != null) {
            oldObj.setNome(obj.getNome());
            oldObj.setEmail(obj.getEmail());
            oldObj.setSobrenome(obj.getSobrenome());
            oldObj.setCpf(obj.getCpf());
            oldObj.setTelefone(obj.getTelefone());
            oldObj.setEspecialidades(obj.getEspecialidades());
            oldObj.setStatus(obj.getStatus());
            
            // Só re-codifica se a senha foi alterada
            if(obj.getSenha() != null && !obj.getSenha().isEmpty()) {
                 oldObj.setSenha(passwordEncoder.encode(obj.getSenha()));
            }
            
            return repository.save(oldObj);
        }
        return null;
    }

    private String getEmailUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}