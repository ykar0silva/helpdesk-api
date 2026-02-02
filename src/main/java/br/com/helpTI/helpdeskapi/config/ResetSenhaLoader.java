	package br.com.helpTI.helpdeskapi.config;
	
	import java.util.List;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.boot.CommandLineRunner;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.security.crypto.password.PasswordEncoder;
	
	import br.com.helpTI.helpdeskapi.domain.Cliente;
	import br.com.helpTI.helpdeskapi.domain.Empresa;
	import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
	import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
	
	@Configuration
	public class ResetSenhaLoader implements CommandLineRunner {
	
	    @Autowired
	    private ClienteRepository clienteRepository;
	
	    @Autowired
	    private EmpresaRepository empresaRepository;
	
	    @Autowired
	    private PasswordEncoder passwordEncoder;
		
		@Override
		public void run(String... args) throws Exception {
		    System.out.println("🔧 [DEV-MODE] FORÇANDO RESET DE SENHAS PARA '123456'...");
		
		    String novaSenhaHash = passwordEncoder.encode("123456");
		
		    // 1. Atualiza Clientes
		    List<Cliente> clientes = clienteRepository.findAll();
		    for (Cliente c : clientes) {
		        c.setSenha(novaSenhaHash);
		        clienteRepository.save(c);
		        System.out.println(" -> Cliente resetado: " + c.getEmail());
		    }
		
		    // 2. Atualiza Empresas
		    List<Empresa> empresas = empresaRepository.findAll();
		    for (Empresa e : empresas) {
		        e.setSenha(novaSenhaHash);
		        empresaRepository.save(e);
		        System.out.println(" -> Empresa resetada: " + e.getEmailResponsavel());
		    }
		    
		    System.out.println("✅ TODAS AS SENHAS AGORA SÃO: 123456");
		}
		
	    private void corrigirSenhasClientes() {
	        List<Cliente> clientes = clienteRepository.findAll();
	        int corrigidos = 0;
	
	        for (Cliente c : clientes) {
	            if (c.getSenha() != null && !c.getSenha().startsWith("$2a$")) {
	                String senhaAntiga = c.getSenha();
	                c.setSenha(passwordEncoder.encode(senhaAntiga));
	                clienteRepository.save(c);
	                corrigidos++;
	                System.out.println("   -> Cliente " + c.getEmail() + ": Senha criptografada com sucesso.");
	            }
	        }
	        if (corrigidos > 0) System.out.println("   --> Total de Clientes corrigidos: " + corrigidos);
	    }
	
	    private void corrigirSenhasEmpresas() {
	        List<Empresa> empresas = empresaRepository.findAll();
	        int corrigidos = 0;
	
	        for (Empresa e : empresas) {
	            // Verifica se a senha da empresa está em texto puro
	            if (e.getSenha() != null && !e.getSenha().startsWith("$2a$")) {
	                String senhaAntiga = e.getSenha();
	                e.setSenha(passwordEncoder.encode(senhaAntiga));
	                empresaRepository.save(e);
	                corrigidos++;
	                System.out.println("   -> Empresa " + e.getNomeFantasia() + ": Senha criptografada com sucesso.");
	            }
	        }
	        if (corrigidos > 0) System.out.println("   --> Total de Empresas corrigidas: " + corrigidos);
	    }
	}