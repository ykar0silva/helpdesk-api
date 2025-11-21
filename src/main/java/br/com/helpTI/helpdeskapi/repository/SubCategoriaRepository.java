package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import br.com.helpTI.helpdeskapi.domain.SubCategoria;
import br.com.helpTI.helpdeskapi.domain.Categoria;

// JpaRepository<Qual classe ele gerencia, Qual o tipo da Chave Primária>
public interface SubCategoriaRepository extends JpaRepository<SubCategoria, Long> {
    
	List<SubCategoria> findByCategoria(Categoria categoria);
}