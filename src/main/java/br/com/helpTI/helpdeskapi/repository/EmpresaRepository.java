package br.com.helpTI.helpdeskapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.Empresa;


public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    
    Optional<Empresa> findByCnpj(String cnpj);

    Optional<Empresa> findByEmailResponsavel(String email);
}