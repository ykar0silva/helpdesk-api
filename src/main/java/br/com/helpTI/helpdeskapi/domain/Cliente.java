package br.com.helpTI.helpdeskapi.domain; 

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

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true) // O e-mail será o login
    private String email;

    @Column(nullable = false)
    private String senha;

    // O nome da empresa onde o cliente trabalha (ex: "Colégio X")
    @Column(nullable = false) 
    private String empresaDoCliente;

    // O perfil de permissão que definimos (USUARIO ou GESTOR)
    @Column(nullable = false)
    private String perfil; // Para o MVP, String é mais simples. (Depois podemos usar Enum)

    // --- Relacionamentos ---

    @ManyToOne // Muitos Clientes pertencem a UMA Empresa (de TI)
    @JoinColumn(name = "empresa_id", nullable = false) // A "Chave Estrangeira"
    private Empresa empresa;
    

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmpresaDoCliente() {
        return empresaDoCliente;
    }

    public void setEmpresaDoCliente(String empresaDoCliente) {
        this.empresaDoCliente = empresaDoCliente;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}