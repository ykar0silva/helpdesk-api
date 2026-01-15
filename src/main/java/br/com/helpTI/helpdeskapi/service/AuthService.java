package br.com.helpTI.helpdeskapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.domain.enums.TipoEmpresa;
import br.com.helpTI.helpdeskapi.dto.CadastroDTO;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;

@Service
public class AuthService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Transactional
    public void registrarCliente(CadastroDTO dto) {
        // 1. Validação básica: E-mail duplicado
        if (clienteRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        // =================================================================================
        // PASSO 1: IDENTIFICAR A EMPRESA RESPONSÁVEL (A "PRESTADORA")
        // =================================================================================
        // Se o DTO trouxe um ID, é um convite de uma Prestadora específica.
        // Se veio nulo, assumimos que é um cadastro orgânico para a Matriz (HelpTI - ID 1).
        Long idPrestadoraResponsavel = (dto.getEmpresaId() != null) ? dto.getEmpresaId() : 1L;

        Empresa prestadora = empresaRepository.findById(idPrestadoraResponsavel)
            .orElseThrow(() -> new RuntimeException("Erro Crítico: Empresa responsável (ID " + idPrestadoraResponsavel + ") não encontrada."));

        // =================================================================================
        // PASSO 2: APLICAR LÓGICA POR TIPO DE CLIENTE (PJ ou PF)
        // =================================================================================

        // --- CENÁRIO A: PESSOA JURÍDICA (Cria uma "Empresa Cliente" no sistema) ---
        if ("JURIDICA".equals(dto.getTipo())) {
            
            Empresa novaEmpresaCliente = new Empresa();
            novaEmpresaCliente.setNomeFantasia(dto.getEmpresaNome());
            novaEmpresaCliente.setCnpj(dto.getDocumento());
            novaEmpresaCliente.setEmailResponsavel(dto.getEmail());
            
            // Define hierarquia: É um Cliente Final, filho da Prestadora identificada acima
            novaEmpresaCliente.setTipoEmpresa(TipoEmpresa.CLIENTE_FINAL);
            novaEmpresaCliente.setPrestadora(prestadora); // <--- VINCULAÇÃO PARENTAL (MUITO IMPORTANTE)
            
            // Configurações padrão
            novaEmpresaCliente.setValorPorChamado(null); 
            novaEmpresaCliente.setCorPrincipal("#000000");
            novaEmpresaCliente.setSenha(encoder.encode("mudar@123")); // Senha padrão
            
            // Salva a Empresa Cliente no banco
            Empresa empresaSalva = empresaRepository.save(novaEmpresaCliente);
            
            // Cria o usuário GESTOR vinculado a essa nova empresa
            criarClienteUsuario(dto, empresaSalva, "GESTOR");
        } 
        
        // --- CENÁRIO B: PESSOA FÍSICA (Cliente Avulso) ---
        else {
            // PF NÃO cria registro na tabela de empresas.
            // O usuário é vinculado diretamente à Prestadora (ou HelpTI) que vai atendê-lo.
            criarClienteUsuario(dto, prestadora, "CLIENTE");
        }
    }

    /**
     * Método auxiliar para criar o registro na tabela de Clientes (Usuários)
     */
    private void criarClienteUsuario(CadastroDTO dto, Empresa empresaVinculada, String perfil) {
        Cliente novoCliente = new Cliente();
        novoCliente.setNome(dto.getNome());
        novoCliente.setEmail(dto.getEmail());
        novoCliente.setSenha(encoder.encode(dto.getSenha()));
        
        novoCliente.setCpf(dto.getDocumento());
        novoCliente.setTelefone(dto.getTelefone());
        
        // =================================================================
        // 🚨 A CORREÇÃO CRUCIAL (Replica o sucesso do SQL) 🚨
        // =================================================================
        
        // 1. Vincula à empresa principal (Coluna empresa_id)
        novoCliente.setEmpresa(empresaVinculada);
        
        // 2. Preenche a coluna redundante obrigatória (Coluna empresa_do_cliente)
        // Antes estava passando String, agora passamos o OBJETO para o Hibernate pegar o ID.
        novoCliente.setEmpresaDoCliente(empresaVinculada.getNomeFantasia());

        // Define o nível de acesso
        novoCliente.setPerfil(perfil); 

        clienteRepository.save(novoCliente);
    }
}