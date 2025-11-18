package br.com.helpTI.helpdeskapi.dto;

import java.math.BigDecimal;

public class TecnicoDashboardDTO {

    private BigDecimal totalGanho; // Total de todos chamados fechados por ele
    private BigDecimal totalAReceber; // O que ainda está com valorPendente > 0

    public TecnicoDashboardDTO(BigDecimal totalGanho, BigDecimal totalAReceber) {
        this.totalGanho = (totalGanho != null) ? totalGanho : BigDecimal.ZERO;
        this.totalAReceber = (totalAReceber != null) ? totalAReceber : BigDecimal.ZERO;
    }

    // Gere Getters e Setters
    public BigDecimal getTotalGanho() {
        return totalGanho;
    }
    public void setTotalGanho(BigDecimal totalGanho) {
        this.totalGanho = totalGanho;
    }
    public BigDecimal getTotalAReceber() {
        return totalAReceber;
    }
    public void setTotalAReceber(BigDecimal totalAReceber) {
        this.totalAReceber = totalAReceber;
    }
}