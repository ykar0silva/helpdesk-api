package br.com.helpTI.helpdeskapi.dto;

import java.math.BigDecimal;

public class PagamentoRequest {
    private BigDecimal valorPago;

    // Getters e Setters
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
}