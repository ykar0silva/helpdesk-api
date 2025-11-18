package br.com.helpTI.helpdeskapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.helpTI.helpdeskapi.dto.AdminDashboardDTO;
import br.com.helpTI.helpdeskapi.dto.TecnicoDashboardDTO;
import br.com.helpTI.helpdeskapi.security.UserDetailsImpl;
import br.com.helpTI.helpdeskapi.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService service;

    // Endpoint para o Dono da Empresa
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // @AuthenticationPrincipal pega o usuário logado (do token JWT)
        // É mais seguro, pois ele só pode ver o SEU próprio dashboard
        AdminDashboardDTO dto = service.getAdminDashboard(userDetails);
        return ResponseEntity.ok(dto);
    }

    // Endpoint para o Técnico
    @GetMapping("/tecnico")
    public ResponseEntity<TecnicoDashboardDTO> getTecnicoDashboard(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // O técnico logado só pode ver o SEU dashboard
        TecnicoDashboardDTO dto = service.getTecnicoDashboard(userDetails);
        return ResponseEntity.ok(dto);
    }
}