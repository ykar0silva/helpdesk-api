package br.com.helpTI.helpdeskapi.dto;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO {
	private String token;
	@Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String novaSenha;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getNovaSenha() {
		return novaSenha;
	}
	public void setNovaSenha(String novaSenha) {
		this.novaSenha = novaSenha;
	}
}
