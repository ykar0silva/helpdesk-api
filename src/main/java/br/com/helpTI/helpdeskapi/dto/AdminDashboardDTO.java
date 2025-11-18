package br.com.helpTI.helpdeskapi.dto;

import java.math.BigDecimal;

public class AdminDashboardDTO {

    private BigDecimal totalPendente;
    // (No futuro, podemos adicionar totalPago, totalChamadosAbertos, etc.)

    public AdminDashboardDTO(BigDecimal totalPendente) {
        this.totalPendente = (totalPendente != null) ? totalPendente : BigDecimal.ZERO;
    }

    // Gere Getters e Setters
    public BigDecimal getTotalPendente() {
        return totalPendente;
    }

    public void setTotalPendente(BigDecimal totalPendente) {
        this.totalPendente = totalPendente;
    }
}