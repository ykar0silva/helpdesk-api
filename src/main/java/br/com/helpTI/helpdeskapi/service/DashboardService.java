package br.com.helpTI.helpdeskapi.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.dto.AdminDashboardDTO;
import br.com.helpTI.helpdeskapi.dto.TecnicoDashboardDTO;
import br.com.helpTI.helpdeskapi.repository.ChamadoRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;
import br.com.helpTI.helpdeskapi.security.UserDetailsImpl;

@Service
public class DashboardService {

    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;

    // Método para o Dashboard do Admin/Dono
    public AdminDashboardDTO getAdminDashboard(UserDetailsImpl userDetails) {
        // Pega o ID da Empresa a partir do usuário logado
        Empresa empresa = empresaRepository.findById(userDetails.getId()).orElse(null);

        BigDecimal totalPendente = chamadoRepository.sumValorPendenteByEmpresa(empresa);

        return new AdminDashboardDTO(totalPendente);
    }

    // Método para o Dashboard do Técnico
    public TecnicoDashboardDTO getTecnicoDashboard(UserDetailsImpl userDetails) {
        // Pega o ID do Técnico a partir do usuário logado
        Tecnico tecnico = tecnicoRepository.findById(userDetails.getId()).orElse(null);

        BigDecimal totalGanho = chamadoRepository.sumValorDoChamadoByTecnico(tecnico);
        BigDecimal totalAReceber = chamadoRepository.sumValorPendenteByTecnico(tecnico);

        return new TecnicoDashboardDTO(totalGanho, totalAReceber);
    }
}