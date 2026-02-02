package br.com.helpTI.helpdeskapi.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.dto.CadastroDTO;
import br.com.helpTI.helpdeskapi.dto.EmailDTO;
import br.com.helpTI.helpdeskapi.dto.LoginRequestDTO;
import br.com.helpTI.helpdeskapi.exception.ObjectNotFoundException;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;
import br.com.helpTI.helpdeskapi.security.JwtTokenService;
import br.com.helpTI.helpdeskapi.security.UserDetailsImpl;
import br.com.helpTI.helpdeskapi.service.AuthService;
import br.com.helpTI.helpdeskapi.service.EmailService;

@RestController
@RequestMapping("/api") 
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthService authService; 

    @Autowired
    private PasswordEncoder encoder;

    // ==========================================
    // MÉTODO 1: LOGIN (ATUALIZADO PARA RETORNAR PERFIL E EMPRESA)
    // ==========================================
    @PostMapping("/login") 
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDto) {
        
        // 1. Autentica
        var usernamePassword = new UsernamePasswordAuthenticationToken(
            loginDto.getEmail(), 
            loginDto.getSenha()
        );

        Authentication auth = authenticationManager.authenticate(usernamePassword);
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        
        // 2. Gera Token
        String token = jwtTokenService.generateToken(userDetails);

        // 3. Monta a Resposta Completa (Map)
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", userDetails.getUsername());
        
        // Pega o perfil da autenticação (Ex: ROLE_ADMIN -> ADMIN)
        String perfil = userDetails.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("CLIENTE");
        
        response.put("perfil", perfil);

        // 4. Busca dados extras (Nome e Empresa) no banco
        String email = userDetails.getUsername();
        
        // Adiciona o empresaId do token (já está no userDetails)
        if (userDetails.getEmpresaId() != null) {
            response.put("empresaId", userDetails.getEmpresaId());
        }
        
        // Verifica se é Técnico
        Optional<Tecnico> tec = tecnicoRepository.findByEmailIgnoreCase(email);
        if (tec.isPresent()) {
            Tecnico t = tec.get();
            response.put("nome", t.getNome());
            response.put("id", t.getId());
            
            if (t.getEmpresa() != null) {
                response.put("empresaNome", t.getEmpresa().getNomeFantasia());
                response.put("empresaId", t.getEmpresa().getId()); // Adiciona empresaId explicitamente
            } else {
                response.put("empresaNome", "HelpTI Matriz");
            }
        } 
        // Verifica se é Cliente
        else {
            Optional<Cliente> cli = clienteRepository.findByEmailIgnoreCase(email);
            if (cli.isPresent()) {
                Cliente c = cli.get();
                response.put("nome", c.getNome());
                response.put("id", c.getId());
                
                if (c.getEmpresa() != null) {
                    response.put("empresaNome", c.getEmpresa().getNomeFantasia());
                    response.put("empresaId", c.getEmpresa().getId()); // Adiciona empresaId explicitamente
                } else {
                    // Fallback para string se não tiver objeto empresa
                    response.put("empresaNome", c.getEmpresaDoCliente() != null ? c.getEmpresaDoCliente() : "Minha Empresa");
                }
            }
        }

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // MÉTODO 2: ESQUECI A SENHA
    // ==========================================
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody EmailDTO emailDTO) {
        String email = emailDTO.getEmail();
        String token = UUID.randomUUID().toString();
        LocalDateTime validade = LocalDateTime.now().plusMinutes(30); 
        
        String link = "http://localhost:5173/recuperar-senha/" + token;

        Optional<Tecnico> tec = tecnicoRepository.findByEmailIgnoreCase(email);
        if (tec.isPresent()) {
            Tecnico tecnico = tec.get();
            tecnico.setTokenRecuperacao(token);
            tecnico.setDataExpiracaoToken(validade);
            tecnicoRepository.save(tecnico);
            
            enviarEmailRecuperacao(tecnico.getNome(), tecnico.getEmail(), link);
            return ResponseEntity.noContent().build();
        }

        Optional<Cliente> cli = clienteRepository.findByEmailIgnoreCase(email);
        if (cli.isPresent()) {
            Cliente cliente = cli.get();
            cliente.setTokenRecuperacao(token);
            cliente.setDataExpiracaoToken(validade);
            clienteRepository.save(cliente);
            
            enviarEmailRecuperacao(cliente.getNome(), cliente.getEmail(), link);
            return ResponseEntity.noContent().build();
        }

        throw new ObjectNotFoundException("E-mail não encontrado!");
    }

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
    // MÉTODO 3: REDEFINIR A SENHA
    // ==========================================
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody br.com.helpTI.helpdeskapi.dto.ResetPasswordDTO dto) {
        String token = dto.getToken();
        String novaSenha = dto.getNovaSenha();

        Optional<Tecnico> tec = tecnicoRepository.findByTokenRecuperacao(token);
        if (tec.isPresent()) {
            Tecnico tecnico = tec.get();
            validarToken(tecnico.getDataExpiracaoToken()); 

            tecnico.setSenha(encoder.encode(novaSenha)); 
            tecnico.setTokenRecuperacao(null); 
            tecnico.setDataExpiracaoToken(null);
            tecnicoRepository.save(tecnico);
            
            return ResponseEntity.ok().build();
        }

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

    private void validarToken(LocalDateTime expiracao) {
        if (expiracao == null || expiracao.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Link expirado! Solicite uma nova recuperação.");
        }
    }
    
    // ==========================================
    // MÉTODO 4: REGISTRO
    // ==========================================
    @PostMapping("/auth/register")
    public ResponseEntity<Void> register(@RequestBody CadastroDTO dto) {
        authService.registrarCliente(dto);
        return ResponseEntity.status(201).build();
    }
}