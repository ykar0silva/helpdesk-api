package br.com.helpTI.helpdeskapi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String solucao;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    private LocalDateTime dataFechamento;

    @Column(nullable = false)
    private String prioridade; // (Ex: "BAIXA", "MEDIA", "ALTA")

    @Column(nullable = false)
    private String status;
 // Valor total base do chamado
    private BigDecimal valorPago = BigDecimal.ZERO; 
    private BigDecimal valorPendente = BigDecimal.ZERO; 
    private BigDecimal valorDoChamado = BigDecimal.ZERO;

    // --- Relacionamentos Principais ---

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false) // Quem abriu
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id") // Quem atendeu (pode ser nulo ao abrir)
    private Tecnico tecnico;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false) // A qual empresa de TI pertence
    private Empresa empresa;

    // --- Relacionamentos de Classificação (preenchidos no fechamento) ---
    
    @ManyToOne
    @JoinColumn(name = "categoria_id") // (pode ser nulo ao abrir)
    private Categoria categoria;
    
    @ManyToOne
    @JoinColumn(name = "subcategoria_id") // (pode ser nulo ao abrir)
    private SubCategoria subCategoria;

    
    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL)
    private List<Nota> notas = new ArrayList<>();
    
   
    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Anexo> anexos = new ArrayList<>();


    public List<Nota> getNotas() {
		return notas;
	}

	public void setNotas(List<Nota> notas) {
		this.notas = notas;
	}

    // "PENDENTE", "PARCIAL", "PAGO"
    private String statusPagamento;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getSolucao() {
		return solucao;
	}

	public void setSolucao(String solucao) {
		this.solucao = solucao;
	}

	public LocalDateTime getDataAbertura() {
		return dataAbertura;
	}

	public void setDataAbertura(LocalDateTime dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	public LocalDateTime getDataFechamento() {
		return dataFechamento;
	}

	public void setDataFechamento(LocalDateTime dataFechamento) {
		this.dataFechamento = dataFechamento;
	}

	public String getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(String prioridade) {
		this.prioridade = prioridade;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Tecnico getTecnico() {
		return tecnico;
	}

	public void setTecnico(Tecnico tecnico) {
		this.tecnico = tecnico;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public SubCategoria getSubCategoria() {
		return subCategoria;
	}

	public void setSubCategoria(SubCategoria subCategoria) {
		this.subCategoria = subCategoria;
	}

	public List<Anexo> getAnexos() {
		return anexos;
	}

	public void setAnexos(List<Anexo> anexos) {
		this.anexos = anexos;
	}

	public BigDecimal getValorDoChamado() {
		return valorDoChamado;
	}

	public void setValorDoChamado(BigDecimal valorDoChamado) {
		this.valorDoChamado = valorDoChamado;
	}

	public BigDecimal getValorPendente() {
		return valorPendente;
	}

	public void setValorPendente(BigDecimal valorPendente) {
		this.valorPendente = valorPendente;
	}

	public String getStatusPagamento() {
		return statusPagamento;
	}

	public void setStatusPagamento(String statusPagamento) {
		this.statusPagamento = statusPagamento;
	}

	public BigDecimal getValorPago() {
		return valorPago;
	}

	public void setValorPago(BigDecimal valorPago) {
		this.valorPago = valorPago;
	} 
    
    

}