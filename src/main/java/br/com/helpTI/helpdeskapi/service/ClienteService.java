package br.com.helpTI.helpdeskapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;
    
    @Autowired 
    private PasswordEncoder passwordEncoder;

    public Cliente findById(Long id) {
        Optional<Cliente> obj = repository.findById(id);
        return obj.orElse(null);
    }

    public List<Cliente> findAll() {
        return repository.findAll();
    }

    public Cliente create(Cliente obj) {
        obj.setId(null);
        obj.setSenha(passwordEncoder.encode(obj.getSenha()));
        return repository.save(obj);
    }

    public Cliente update(Long id, Cliente obj) {
        Cliente oldObj = findById(id);
        if (oldObj != null) {
            oldObj.setNome(obj.getNome());
            oldObj.setEmail(obj.getEmail());
            oldObj.setEmpresaDoCliente(obj.getEmpresaDoCliente());
            oldObj.setPerfil(obj.getPerfil());
            
            if(obj.getSenha() != null && !obj.getSenha().isEmpty()) {
                 oldObj.setSenha(obj.getSenha());
            }
            return repository.save(oldObj);
        }
        return null;
    }
}