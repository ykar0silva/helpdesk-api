package br.com.helpTI.helpdeskapi.dto;

public class TokenResponseDTO {

    private String email;
    private String token;

    public TokenResponseDTO(String email, String token) {
        this.email = email;
        this.token = token;
    }

    // Getters e Setters
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}