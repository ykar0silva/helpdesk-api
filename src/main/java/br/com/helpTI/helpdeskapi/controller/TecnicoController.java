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

import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.service.TecnicoService;

@RestController
@RequestMapping(value = "/api/tecnicos") // URL base para técnicos
public class TecnicoController {

    @Autowired
    private TecnicoService service;

    // GET /api/tecnicos/1
    @GetMapping(value = "/{id}")
    public ResponseEntity<Tecnico> findById(@PathVariable Long id) {
        Tecnico obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    // GET /api/tecnicos
    @GetMapping
    public ResponseEntity<List<Tecnico>> findAll() {
        List<Tecnico> list = service.findAll();
        return ResponseEntity.ok().body(list);
    }

    // POST /api/tecnicos
    @PostMapping
    public ResponseEntity<Tecnico> create(@RequestBody Tecnico obj) {
        Tecnico newObj = service.create(obj);
        return ResponseEntity.status(201).body(newObj);
    }

    // PUT /api/tecnicos/1
    @PutMapping(value = "/{id}")
    public ResponseEntity<Tecnico> update(@PathVariable Long id, @RequestBody Tecnico obj) {
        Tecnico updatedObj = service.update(id, obj);
        return ResponseEntity.ok().body(updatedObj);
    }
    
    @GetMapping(value = "/ativos")
    public ResponseEntity<List<Tecnico>> findAllAtivos() {
        List<Tecnico> list = service.findAllAtivos();
        return ResponseEntity.ok().body(list);
    }
}
