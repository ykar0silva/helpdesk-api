package br.com.helpTI.helpdeskapi.controller;

import java.net.URI;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.service.TecnicoService;

@RestController
@RequestMapping(value = "/api/tecnicos")
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
        // Agora chama o service passando a Entidade Tecnico, compatível com a sua alteração anterior
        Tecnico newObj = service.create(obj);
        
        // Padrão REST: Retorna a URL do novo recurso no Header 'Location'
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(newObj.getId()).toUri();
                
        return ResponseEntity.created(uri).body(newObj);
    }

    // PUT /api/tecnicos/1
    @PutMapping(value = "/{id}")
    public ResponseEntity<Tecnico> update(@PathVariable Long id, @RequestBody Tecnico obj) {
        Tecnico updatedObj = service.update(id, obj);
        return ResponseEntity.ok().body(updatedObj);
    }
    
    // GET /api/tecnicos/ativos
    @GetMapping(value = "/ativos")
    public ResponseEntity<List<Tecnico>> findAllAtivos() {
        List<Tecnico> list = service.findAllAtivos();
        return ResponseEntity.ok().body(list);
    }
}