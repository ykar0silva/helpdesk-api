package br.com.helpTI.helpdeskapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.helpTI.helpdeskapi.dto.LoginRequestDTO;
import br.com.helpTI.helpdeskapi.dto.TokenResponseDTO;
import br.com.helpTI.helpdeskapi.security.JwtTokenService;
import br.com.helpTI.helpdeskapi.security.UserDetailsImpl;

@RestController
@RequestMapping("/api/login")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager; // O "cérebro" do login

    @Autowired
    private JwtTokenService jwtTokenService; // O "criador" de tokens

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDto) {

        // 1. Cria um "token" de autenticação com o e-mail e senha
        var usernamePassword = new UsernamePasswordAuthenticationToken(
            loginDto.getEmail(), 
            loginDto.getSenha()
        );

        // 2. O AuthenticationManager vai:
        //    a. Chamar o nosso UserDetailsServiceIm
        //    b. Buscar o utilizador no banco
        //    c. Comparar a senha (com BCrypt)
        //    d. Se falhar, ele lança uma exceção (que trataremos depois)
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        // 3. Se chegou aqui, o login FOI UM SUCESSO.

        // 4. Pega os detalhes do utilizador que foi autenticado
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        // 5. Gera o token JWT para este utilizador
        String token = jwtTokenService.generateToken(userDetails);

        // 6. Retorna o token para o cliente
        return ResponseEntity.ok(new TokenResponseDTO(userDetails.getUsername(), token));
    }
}