package br.com.helpTI.helpdeskapi.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
    private LocalDateTime dataAbertura = LocalDateTime.now(); // Garante a data ao instanciar

    private LocalDateTime dataFechamento;

    @Column(nullable = false)
    private String prioridade;
    
    // GPS (Varchar 255 é suficiente)
    private String latitude;
    private String longitude;

    @Column(nullable = false)
    private String status;

    // --- CORREÇÃO MVP: CATEGORIA COMO STRING ---
    // Removemos o @ManyToOne para aceitar o texto "HARDWARE" direto do frontend
    private String categoria;

    // --- FINANCEIRO ---
    // precision=10, scale=2 garante formato monetário no banco (ex: 99999999.99)
    @Column(precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO; 
    
    @Column(precision = 10, scale = 2)
    private BigDecimal valorPendente = BigDecimal.ZERO; 
    
    @Column(precision = 10, scale = 2)
    private BigDecimal valorDoChamado = BigDecimal.ZERO;

    // "PENDENTE", "PARCIAL", "PAGO"
    private String statusPagamento;

    // --- RELACIONAMENTOS ---

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    @ManyToOne
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // Subcategoria removida temporariamente para simplificar MVP
    // private SubCategoria subCategoria;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL)
    private List<Nota> notas = new ArrayList<>();
    
    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Anexo> anexos = new ArrayList<>();

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getSolucao() { return solucao; }
    public void setSolucao(String solucao) { this.solucao = solucao; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    // Getter e Setter da Categoria (Agora String)
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public BigDecimal getValorPendente() { return valorPendente; }
    public void setValorPendente(BigDecimal valorPendente) { this.valorPendente = valorPendente; }

    public BigDecimal getValorDoChamado() { return valorDoChamado; }
    public void setValorDoChamado(BigDecimal valorDoChamado) { this.valorDoChamado = valorDoChamado; }

    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Tecnico getTecnico() { return tecnico; }
    public void setTecnico(Tecnico tecnico) { this.tecnico = tecnico; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public List<Nota> getNotas() { return notas; }
    public void setNotas(List<Nota> notas) { this.notas = notas; }

    public List<Anexo> getAnexos() { return anexos; }
    public void setAnexos(List<Anexo> anexos) { this.anexos = anexos; }
}