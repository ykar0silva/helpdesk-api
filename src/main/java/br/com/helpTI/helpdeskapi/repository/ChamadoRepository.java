package br.com.helpTI.helpdeskapi.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.helpTI.helpdeskapi.domain.Chamado;
import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.domain.Tecnico;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    // Busca todos por empresa
    List<Chamado> findAllByEmpresa(Empresa empresa);

    // Métodos para o Dashboard Financeiro (Empresa)
    List<Chamado> findAllByEmpresaAndValorPendenteGreaterThanOrderByDataFechamentoAsc(Empresa empresa, BigDecimal valorPendente);

    // Métodos para o Dashboard Financeiro (Técnico)
    List<Chamado> findAllByTecnicoAndValorPendenteGreaterThanOrderByDataFechamentoAsc(Tecnico tecnico, BigDecimal valorPendente);

    // --- MÉTODO QUE FALTAVA (Correção do Erro) ---
    // Isso resolve o erro "undefined method findAllByTecnicoId" no Service
    List<Chamado> findAllByTecnicoId(Long tecnicoId);
    
    // Busca apenas os ativos de um técnico específico (Customizado)
    @Query("SELECT c FROM Chamado c WHERE c.tecnico.id = :tecnicoId AND c.status <> 'FECHADO'")
    List<Chamado> findActiveByTecnicoId(@Param("tecnicoId") Long tecnicoId);

    // 1. Para o Admin: Busca todos os chamados ATIVOS da Empresa
    List<Chamado> findAllByEmpresaAndStatusNot(Empresa empresa, String status);

    // --- SOMAS E TOTALIZADORES ---

    @Query("SELECT SUM(c.valorPendente) FROM Chamado c WHERE c.empresa = :empresa AND c.statusPagamento <> 'PAGO'")
    BigDecimal sumValorPendenteByEmpresa(@Param("empresa") Empresa empresa);

    @Query("SELECT SUM(c.valorDoChamado) FROM Chamado c WHERE c.tecnico = :tecnico AND c.status = 'FECHADO'")
    BigDecimal sumValorDoChamadoByTecnico(@Param("tecnico") Tecnico tecnico);

    @Query("SELECT SUM(c.valorPendente) FROM Chamado c WHERE c.tecnico = :tecnico AND c.statusPagamento <> 'PAGO'")
    BigDecimal sumValorPendenteByTecnico(@Param("tecnico") Tecnico tecnico);

    // --- QUERIES DE DASHBOARD (Limpos de caracteres inválidos) ---

    // 1. Para o ADMIN (Empresa): Abertos OU Fechados recentemente
    @Query("SELECT c FROM Chamado c WHERE c.empresa = :empresa AND (c.status <> 'FECHADO' OR c.dataFechamento >= :dataLimite)")
    List<Chamado> findChamadosDashboardEmpresa(@Param("empresa") Empresa empresa, @Param("dataLimite") LocalDateTime dataLimite);

    // 2. Para o TÉCNICO (Pessoal): Abertos OU Fechados recentemente
    @Query("SELECT c FROM Chamado c WHERE c.tecnico = :tecnico AND (c.status <> 'FECHADO' OR c.dataFechamento >= :dataLimite)")
    List<Chamado> findChamadosDashboardTecnico(@Param("tecnico") Tecnico tecnico, @Param("dataLimite") LocalDateTime dataLimite);

    List<Chamado> findAllByClienteId(Long clienteId);
}