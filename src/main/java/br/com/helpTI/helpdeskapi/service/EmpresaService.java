package br.com.helpTI.helpdeskapi.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository repository;

    @Autowired // 1. INJETE O CRIPTOGRAFADOR
    private PasswordEncoder passwordEncoder;
    
    public Empresa findById(Long id) {
        Optional<Empresa> obj = repository.findById(id);
        // Retorna o objeto ou null se não achar (depois trataremos exceções melhor)
        return obj.orElse(null); 
    }

    public List<Empresa> findAll() {
        return repository.findAll();
    }

    public Empresa create(Empresa obj) {
    	obj.setSenha(passwordEncoder.encode(obj.getSenha()));
        return repository.save(obj);
    }

    public Empresa update(Long id, Empresa obj) {
        Empresa oldObj = findById(id);
        if(oldObj != null) {
            oldObj.setNomeFantasia(obj.getNomeFantasia());
            oldObj.setEmailResponsavel(obj.getEmailResponsavel());
            return repository.save(oldObj);
        }
        return null;
    }
}