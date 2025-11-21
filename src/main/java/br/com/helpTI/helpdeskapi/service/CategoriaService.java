package br.com.helpTI.helpdeskapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importe

import br.com.helpTI.helpdeskapi.domain.Categoria;
import br.com.helpTI.helpdeskapi.domain.SubCategoria;
import br.com.helpTI.helpdeskapi.repository.CategoriaRepository;
import br.com.helpTI.helpdeskapi.repository.SubCategoriaRepository;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private SubCategoriaRepository subCategoriaRepository;
    
    // --- MÉTODOS PARA CATEGORIA ---
    
    public List<Categoria> findAllCategorias() {
        return categoriaRepository.findAll();
    }
    
    @Transactional
    public Categoria createCategoria(Categoria obj) {
        obj.setId(null);
        // Opcional: Adicionar validação de nome
        return categoriaRepository.save(obj);
    }
    
    // --- MÉTODOS PARA SUBCATEGORIA ---
    
    public List<SubCategoria> findAllSubCategorias() {
        return subCategoriaRepository.findAll();
    }
    
    @Transactional
    public SubCategoria createSubCategoria(SubCategoria obj) {
        obj.setId(null);
        // Garante que a Categoria pai existe antes de salvar a Subcategoria
        Categoria categoriaPai = categoriaRepository.findById(obj.getCategoria().getId())
            .orElseThrow(() -> new RuntimeException("Categoria pai não encontrada")); // Tratamento básico de erro
        
        obj.setCategoria(categoriaPai);
        return subCategoriaRepository.save(obj);
    }
    
    public List<SubCategoria> findSubCategoriasByCategoria(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
            .orElseThrow(() -> new RuntimeException("Categoria pai não encontrada"));
        
        return subCategoriaRepository.findByCategoria(categoria);
    }
}