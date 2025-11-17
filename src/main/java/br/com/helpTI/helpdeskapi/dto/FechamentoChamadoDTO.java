package br.com.helpTI.helpdeskapi.dto;

// Esta classe é um "molde" simples para receber os dados
// do técnico quando ele fechar um chamado.
// (Use Lombok @Getter/@Setter aqui se quiser)
public class FechamentoChamadoDTO {

    private String solucao;
    private Long categoriaId;
    private Long subCategoriaId;

    // Getters e Setters
    public String getSolucao() {
        return solucao;
    }
    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }
    public Long getCategoriaId() {
        return categoriaId;
    }
    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
    public Long getSubCategoriaId() {
        return subCategoriaId;
    }
    public void setSubCategoriaId(Long subCategoriaId) {
        this.subCategoriaId = subCategoriaId;
    }
}