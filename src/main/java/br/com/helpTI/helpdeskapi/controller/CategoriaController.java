package br.com.helpTI.helpdeskapi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Importe tudo

import br.com.helpTI.helpdeskapi.domain.Categoria;
import br.com.helpTI.helpdeskapi.domain.SubCategoria;
import br.com.helpTI.helpdeskapi.service.CategoriaService;

@RestController
@RequestMapping(value = "/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService service;

    // GET /api/categorias (Listar todas as categorias)
    @GetMapping
    public ResponseEntity<List<Categoria>> findAllCategorias() {
        List<Categoria> list = service.findAllCategorias();
        return ResponseEntity.ok().body(list);
    }
    
    // POST /api/categorias (Criar nova categoria)
    @PostMapping
    public ResponseEntity<Categoria> createCategoria(@RequestBody Categoria obj) {
        Categoria newObj = service.createCategoria(obj);
        return ResponseEntity.status(201).body(newObj);
    }

    // GET /api/categorias/subcategorias (Listar todas as subcategorias)
    @GetMapping(value = "/subcategorias")
    public ResponseEntity<List<SubCategoria>> findAllSubCategorias() {
        List<SubCategoria> list = service.findAllSubCategorias();
        return ResponseEntity.ok().body(list);
    }

    // POST /api/categorias/subcategorias (Criar nova subcategoria)
    @PostMapping(value = "/subcategorias")
    public ResponseEntity<SubCategoria> createSubCategoria(@RequestBody SubCategoria obj) {
        SubCategoria newObj = service.createSubCategoria(obj);
        return ResponseEntity.status(201).body(newObj);
    }
    
    // GET /api/categorias/1/subcategorias (Listar por ID de categoria)
    @GetMapping(value = "/{categoriaId}/subcategorias")
    public ResponseEntity<List<SubCategoria>> findSubCategoriasByCategoria(@PathVariable Long categoriaId) {
        List<SubCategoria> list = service.findSubCategoriasByCategoria(categoriaId);
        return ResponseEntity.ok().body(list);
    }
}