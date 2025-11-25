package br.com.helpTI.helpdeskapi.service;

import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;

@Service
public class TecnicoService {

    @Autowired
    private TecnicoRepository repository;

    @Autowired 
    private PasswordEncoder passwordEncoder;
    
    public Tecnico findById(Long id) {
        Optional<Tecnico> obj = repository.findById(id);
        return obj.orElse(null);
    }

    public List<Tecnico> findAll() {
        return repository.findAll();
    }

    public Tecnico create(Tecnico obj) {
        obj.setId(null); 
        obj.setSenha(passwordEncoder.encode(obj.getSenha()));
        return repository.save(obj);
    }
    
    
    public Tecnico update(Long id, Tecnico obj) {
        Tecnico oldObj = findById(id);
        if (oldObj != null) {
            oldObj.setNome(obj.getNome());
            oldObj.setEmail(obj.getEmail());
            // Se a senha foi enviada, atualiza. Se não, mantém a antiga.
            if(obj.getSenha() != null && !obj.getSenha().isEmpty()) {
                 oldObj.setSenha(obj.getSenha());
            }
            return repository.save(oldObj);
        }
        return null;
    }
}