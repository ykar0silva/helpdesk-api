package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.Categoria;

// JpaRepository<Qual classe ele gerencia, Qual o tipo da Chave Primária>
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    // Por enquanto, só o JpaRepository básico é o suficiente.
    // O Spring já nos dará o findById(), findAll(), save(), delete().
}