package br.com.helpTI.helpdeskapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.helpTI.helpdeskapi.domain.Chamado;
import java.util.List;
import br.com.helpTI.helpdeskapi.domain.Empresa;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {


    List<Chamado> findAllByEmpresa(Empresa empresa);
    

    List<Chamado> findAllByEmpresaAndValorPendenteGreaterThanOrderByDataFechamentoAsc(
        Empresa empresa, 
        java.math.BigDecimal valorPendente
    );
}