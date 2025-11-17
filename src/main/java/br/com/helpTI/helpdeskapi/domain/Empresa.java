package br.com.helpTI.helpdeskapi.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// --- Configuração do JPA / Hibernate ---

@Entity // 1. Diz ao JPA que esta classe é uma entidade (uma tabela)
@Table(name = "empresas") // 2. Define o nome da tabela no banco
public class Empresa {
	
	
	

    @Id // 3. Marca este campo como a Chave Primária (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. Define o auto-incremento
    private Long id;

    @Column(nullable = false) // 5. 'nullable=false' significa que é 'NOT NULL' (obrigatório)
    private String nomeFantasia;

    @Column(nullable = false, unique = true) // 6. 'unique=true' garante que não haja CNPJs duplicados
    private String cnpj;

    @Column(nullable = false)
    private String emailResponsavel;
    
    @Column(nullable = false)
    private String senha;
    
    // 7. A logo (White Label)
    private String logoUrl; 

    // 8. A cor (White Label)
    private String corPrincipal; 

    // --- Configurações Financeiras ---

    // 9. Usamos BigDecimal para dinheiro (muito mais preciso que Double)
    @Column(nullable = false)
    private BigDecimal valorPorChamado;


    // --- Getters e Setters ---
    // (Se você instalou o Lombok, pode pular isso e só adicionar @Getter e @Setter em cima da classe)

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

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
    
}