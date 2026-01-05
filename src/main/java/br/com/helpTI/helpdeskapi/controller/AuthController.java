package br.com.helpTI.helpdeskapi.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.dto.EmailDTO; // Certifique-se que criou este DTO
import br.com.helpTI.helpdeskapi.dto.LoginRequestDTO;
import br.com.helpTI.helpdeskapi.dto.TokenResponseDTO;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;
import br.com.helpTI.helpdeskapi.security.JwtTokenService;
import br.com.helpTI.helpdeskapi.security.UserDetailsImpl;
import br.com.helpTI.helpdeskapi.service.EmailService;
import br.com.helpTI.helpdeskapi.exception.ObjectNotFoundException;

@RestController
@RequestMapping("/api") // Mudei de "/api/login" para "/api" para suportar várias rotas
public class AuthController {

    // --- DEPENDÊNCIAS DE LOGIN ---
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    // --- DEPENDÊNCIAS DE RECUPERAÇÃO DE SENHA ---
    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder encoder;
    // ==========================================
    // MÉTODO 1: LOGIN (URL: /api/login)
    // ==========================================
    @PostMapping("/login") 
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDto) {
        
        // 1. Cria o token de autenticação
        var usernamePassword = new UsernamePasswordAuthenticationToken(
            loginDto.getEmail(), 
            loginDto.getSenha()
        );

        // 2. Autentica
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        // 3. Pega o usuário e gera o JWT
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtTokenService.generateToken(userDetails);

        // 4. Retorna
        return ResponseEntity.ok(new TokenResponseDTO(userDetails.getUsername(), token));
    }

    // ==========================================
    // MÉTODO 2: ESQUECI A SENHA (URL: /api/forgot-password)
    // ==========================================
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody EmailDTO emailDTO) {
        String email = emailDTO.getEmail();
        String token = UUID.randomUUID().toString();
        // Token válido por 30 minutos
        LocalDateTime validade = LocalDateTime.now().plusMinutes(30); 
        
        // Link apontando para o seu Frontend (Porta 5173)
        // OBS: Se o seu frontend estiver em outra porta, altere aqui.
        String link = "http://localhost:5173/recuperar-senha/" + token;

        // 1. TENTA ACHAR COMO TÉCNICO
        Optional<Tecnico> tec = tecnicoRepository.findByEmailIgnoreCase(email);
        if (tec.isPresent()) {
            Tecnico tecnico = tec.get();
            tecnico.setTokenRecuperacao(token);
            tecnico.setDataExpiracaoToken(validade);
            tecnicoRepository.save(tecnico);
            
            enviarEmailRecuperacao(tecnico.getNome(), tecnico.getEmail(), link);
            return ResponseEntity.noContent().build();
        }

        // 2. SE NÃO ACHOU, TENTA ACHAR COMO CLIENTE
        Optional<Cliente> cli = clienteRepository.findByEmailIgnoreCase(email);
        if (cli.isPresent()) {
            Cliente cliente = cli.get();
            cliente.setTokenRecuperacao(token);
            cliente.setDataExpiracaoToken(validade);
            clienteRepository.save(cliente);
            
            enviarEmailRecuperacao(cliente.getNome(), cliente.getEmail(), link);
            return ResponseEntity.noContent().build();
        }

        // Se chegou aqui, o email não existe
        throw new ObjectNotFoundException("E-mail não encontrado!");
    }

    // Método auxiliar privado para enviar o e-mail
    private void enviarEmailRecuperacao(String nome, String email, String link) {
        String mensagem = "Olá, " + nome + "!\n\n"
                + "Você solicitou a recuperação de senha.\n"
                + "Clique no link abaixo para criar uma nova senha:\n\n"
                + link + "\n\n"
                + "Este link expira em 30 minutos.\n"
                + "Se não foi você, ignore esta mensagem.";
        
        emailService.enviarEmailTexto(email, "Recuperação de Senha - HelpTI", mensagem);
    }
    
 // ==========================================
    // MÉTODO 3: REDEFINIR A SENHA (URL: /api/reset-password)
    // ==========================================
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody br.com.helpTI.helpdeskapi.dto.ResetPasswordDTO dto) {
        String token = dto.getToken();
        String novaSenha = dto.getNovaSenha();

        // 1. PROCURA NO TÉCNICO
        Optional<Tecnico> tec = tecnicoRepository.findByTokenRecuperacao(token);
        if (tec.isPresent()) {
            Tecnico tecnico = tec.get();
            validarToken(tecnico.getDataExpiracaoToken()); // Verifica validade

            tecnico.setSenha(encoder.encode(novaSenha)); // Criptografa
            tecnico.setTokenRecuperacao(null); // Queima o token (uso único)
            tecnico.setDataExpiracaoToken(null);
            tecnicoRepository.save(tecnico);
            
            return ResponseEntity.ok().build();
        }

        // 2. PROCURA NO CLIENTE
        Optional<Cliente> cli = clienteRepository.findByTokenRecuperacao(token);
        if (cli.isPresent()) {
            Cliente cliente = cli.get();
            validarToken(cliente.getDataExpiracaoToken());

            cliente.setSenha(encoder.encode(novaSenha));
            cliente.setTokenRecuperacao(null);
            cliente.setDataExpiracaoToken(null);
            clienteRepository.save(cliente);
            
            return ResponseEntity.ok().build();
        }

        throw new RuntimeException("Token inválido ou expirado!");
    }

    // Validação auxiliar
    private void validarToken(LocalDateTime expiracao) {
        if (expiracao == null || expiracao.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link expirado! Solicite uma nova recuperação.");
        }
    }
}