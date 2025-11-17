package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    
    Optional<Tecnico> findByEmail(String email);
}