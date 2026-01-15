package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
	
	Optional<Cliente> findByTokenRecuperacao(String token);
	Optional<Cliente> findByEmailIgnoreCase(String email);
	List<Cliente> findAllByEmpresaId(Long empresaId);
}