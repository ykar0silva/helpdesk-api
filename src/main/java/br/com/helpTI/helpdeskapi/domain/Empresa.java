package br.com.helpTI.helpdeskapi.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import br.com.helpTI.helpdeskapi.domain.enums.TipoEmpresa; 

@Entity
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeFantasia;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @Column(nullable = false)
    private String emailResponsavel;
    
    @Column(nullable = false)
    private String senha;
    
    // --- Configuração White Label ---
    private String logoUrl; 
    private String corPrincipal; 

    // --- Configuração Financeira ---
    @Column(nullable = true)
    private BigDecimal valorPorChamado;

    // =================================================================
    // NOVOS CAMPOS PARA A LÓGICA DE NEGÓCIO HIERÁRQUICA
    // =================================================================

    // 1. Define quem é essa empresa (Matriz, Prestadora ou Cliente Final)
    // Armazena como TEXTO no banco ("PRESTADORA", "CLIENTE_FINAL") para facilitar leitura
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEmpresa tipoEmpresa;

    // 2. O Elo da Corrente (Auto-relacionamento)
    // Se esta for uma "Empresa Cliente" (Padaria), este campo aponta para a "Prestadora" (TechSolutions).
    // Se for a Matriz ou uma Prestadora independente, este campo pode ficar NULL.
    @ManyToOne
    @JoinColumn(name = "prestadora_id") 
    private Empresa prestadora;


    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEmailResponsavel() {
        return emailResponsavel;
    }

    public void setEmailResponsavel(String emailResponsavel) {
        this.emailResponsavel = emailResponsavel;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCorPrincipal() {
        return corPrincipal;
    }

    public void setCorPrincipal(String corPrincipal) {
        this.corPrincipal = corPrincipal;
    }

    public BigDecimal getValorPorChamado() {
        return valorPorChamado;
    }

    public void setValorPorChamado(BigDecimal valorPorChamado) {
        this.valorPorChamado = valorPorChamado;
    }

    // --- Novos Getters e Setters ---

    public TipoEmpresa getTipoEmpresa() {
        return tipoEmpresa;
    }

    public void setTipoEmpresa(TipoEmpresa tipoEmpresa) {
        this.tipoEmpresa = tipoEmpresa;
    }

    public Empresa getPrestadora() {
        return prestadora;
    }

    public void setPrestadora(Empresa prestadora) {
        this.prestadora = prestadora;
    }
}