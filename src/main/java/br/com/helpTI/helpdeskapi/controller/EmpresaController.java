package br.com.helpTI.helpdeskapi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.service.EmpresaService;

@RestController // 1. Marca a classe como um Controller REST
@RequestMapping(value = "/api/empresas") // 2. Define a URL base (ex: http://localhost:8080/api/empresas)
public class EmpresaController {

    @Autowired
    private EmpresaService service;

    // Endpoint para BUSCAR UMA empresa pelo ID
    // GET /api/empresas/1
    @GetMapping(value = "/{id}")
    public ResponseEntity<Empresa> findById(@PathVariable Long id) {
        Empresa obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    // Endpoint para BUSCAR TODAS as empresas
    // GET /api/empresas
    @GetMapping
    public ResponseEntity<List<Empresa>> findAll() {
        List<Empresa> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }

    // Endpoint para CRIAR uma nova empresa
    // POST /api/empresas
    @PostMapping
    public ResponseEntity<Empresa> create(@RequestBody Empresa obj) {
        Empresa newObj = service.create(obj);
        // Retorna 201 Created com a nova empresa no corpo
        return ResponseEntity.status(201).body(newObj); 
    }

    // Endpoint para ATUALIZAR uma empresa
    // PUT /api/empresas/1
    @PutMapping(value = "/{id}")
    public ResponseEntity<Empresa> update(@PathVariable Long id, @RequestBody Empresa obj) {
        Empresa updatedObj = service.update(id, obj);
        return ResponseEntity.ok().body(updatedObj);
    }
}
