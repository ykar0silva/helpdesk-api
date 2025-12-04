package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.Chamado;
import java.util.List;
import br.com.helpTI.helpdeskapi.domain.Empresa;
import org.springframework.data.jpa.repository.Query; // IMPORTE
import org.springframework.data.repository.query.Param; // IMPORTE
import java.math.BigDecimal; // IMPORTE
import java.time.LocalDateTime;

import br.com.helpTI.helpdeskapi.domain.Tecnico; // IMPORTE

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

	List<Chamado> findAllByEmpresa(Empresa empresa);

	List<Chamado> findAllByEmpresaAndValorPendenteGreaterThanOrderByDataFechamentoAsc(Empresa empresa,
			java.math.BigDecimal valorPendente);

	List<Chamado> findAllByTecnicoAndValorPendenteGreaterThanOrderByDataFechamentoAsc(Tecnico tecnico,
			java.math.BigDecimal valorPendente);

	// 1. Para o Admin: Soma tudo que está pendente na empresa
	@Query("SELECT SUM(c.valorPendente) FROM Chamado c WHERE c.empresa = :empresa AND c.statusPagamento <> 'PAGO'")
	BigDecimal sumValorPendenteByEmpresa(@Param("empresa") Empresa empresa);

	// 2. Para o Técnico: Soma o valor total de tudo que ele fechou
	@Query("SELECT SUM(c.valorDoChamado) FROM Chamado c WHERE c.tecnico = :tecnico AND c.status = 'FECHADO'")
	BigDecimal sumValorDoChamadoByTecnico(@Param("tecnico") Tecnico tecnico);

	// 3. Para o Técnico: Soma o que ainda falta ele receber
	@Query("SELECT SUM(c.valorPendente) FROM Chamado c WHERE c.tecnico = :tecnico AND c.statusPagamento <> 'PAGO'")
	BigDecimal sumValorPendenteByTecnico(@Param("tecnico") Tecnico tecnico);

	// ChamadoRepository.java

	// 1. Para o Admin: Busca todos os chamados ATIVOS da Empresa (Status !=
	// FECHADO)
	List<Chamado> findAllByEmpresaAndStatusNot(Empresa empresa, String status);

	@Query("SELECT c FROM Chamado c WHERE c.tecnico.id = :tecnicoId AND c.status <> 'FECHADO'")
	List<Chamado> findActiveByTecnicoId(@Param("tecnicoId") Long tecnicoId);
	
	// 1. Para o ADMIN (Empresa): Abertos OU Fechados recentemente
    @Query("SELECT c FROM Chamado c WHERE c.empresa = :empresa AND (c.status <> 'FECHADO' OR c.dataFechamento >= :dataLimite)")
    List<Chamado> findChamadosDashboardEmpresa(@Param("empresa") Empresa empresa, @Param("dataLimite") LocalDateTime dataLimite);

    // 2. Para o TÉCNICO (Pessoal): Abertos OU Fechados recentemente
    @Query("SELECT c FROM Chamado c WHERE c.tecnico = :tecnico AND (c.status <> 'FECHADO' OR c.dataFechamento >= :dataLimite)")
    List<Chamado> findChamadosDashboardTecnico(@Param("tecnico") Tecnico tecnico, @Param("dataLimite") LocalDateTime dataLimite);
}