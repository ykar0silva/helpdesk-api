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

import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.service.ClienteService;

@RestController
@RequestMapping(value = "/api/clientes") // URL base para clientes
public class ClienteController {

    @Autowired
    private ClienteService service;

    // GET /api/clientes/1
    @GetMapping(value = "/{id}")
    public ResponseEntity<Cliente> findById(@PathVariable Long id) {
        Cliente obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    // GET /api/clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> findAll() {
        List<Cliente> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }

    // POST /api/clientes
    @PostMapping
    public ResponseEntity<Cliente> create(@RequestBody Cliente obj) {
        Cliente newObj = service.create(obj);
        return ResponseEntity.status(201).body(newObj);
    }

    // PUT /api/clientes/1
    @PutMapping(value = "/{id}")
    public ResponseEntity<Cliente> update(@PathVariable Long id, @RequestBody Cliente obj) {
        Cliente updatedObj = service.update(id, obj);
        return ResponseEntity.ok().body(updatedObj);
    }
}