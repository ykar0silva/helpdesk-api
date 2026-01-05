package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.Tecnico;

import java.util.List;
import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
	Optional<Tecnico> findByEmail(String email);
    Optional<Tecnico> findByEmailIgnoreCase(String email);
    Optional<Tecnico> findByTokenRecuperacao(String token);
	List<Tecnico> findByStatus(String status);
}