package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.SubCategoria;

// JpaRepository<Qual classe ele gerencia, Qual o tipo da Chave Primária>
public interface SubCategoriaRepository extends JpaRepository<SubCategoria, Long> {
    
    // Também não precisamos de métodos customizados por agora.
    
}