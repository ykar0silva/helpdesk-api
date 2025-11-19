package br.com.helpTI.helpdeskapi.domain;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notas")
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String texto;

    private LocalDateTime dataCriacao;

    // Quem escreveu a nota? (Pode ser Técnico, Cliente ou Admin)
    // Para simplificar, guardamos o nome e o tipo como string
    private String autorNome;
    private String autorTipo; // "TECNICO", "CLIENTE", "SISTEMA"

    @ManyToOne
    @JoinColumn(name = "chamado_id", nullable = false)
    @JsonIgnore
    private Chamado chamado;

    // Construtor padrão
    public Nota() {
        this.dataCriacao = LocalDateTime.now();
    }

    // Getters e Setters... (Gere eles na IDE)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getAutorNome() { return autorNome; }
    public void setAutorNome(String autorNome) { this.autorNome = autorNome; }
    public String getAutorTipo() { return autorTipo; }
    public void setAutorTipo(String autorTipo) { this.autorTipo = autorTipo; }
    public Chamado getChamado() { return chamado; }
    public void setChamado(Chamado chamado) { this.chamado = chamado; }
}