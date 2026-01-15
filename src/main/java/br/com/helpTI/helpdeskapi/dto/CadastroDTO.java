package br.com.helpTI.helpdeskapi.dto;

import java.io.Serializable;

public class CadastroDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private String tipo; // "FISICA" ou "JURIDICA"
    private String documento; // CPF ou CNPJ
    private String empresaNome; // Só vem se for JURIDICA

    // --- NOVO CAMPO ---
    // Recebe o ID da Prestadora vindo da URL (?empresa=X)
    // Se vier nulo, o AuthService entenderá que é campanha da Matriz (HelpTI)
    private Long empresaId; 

    public CadastroDTO() {}

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getEmpresaNome() { return empresaNome; }
    public void setEmpresaNome(String empresaNome) { this.empresaNome = empresaNome; }

    // Getter e Setter do Novo Campo
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
}