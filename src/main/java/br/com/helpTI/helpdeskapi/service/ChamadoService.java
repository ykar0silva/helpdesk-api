package br.com.helpTI.helpdeskapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.helpTI.helpdeskapi.domain.Anexo;
import br.com.helpTI.helpdeskapi.domain.Categoria;
import br.com.helpTI.helpdeskapi.domain.Chamado;
import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.domain.Nota;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.dto.FechamentoChamadoDTO;
import br.com.helpTI.helpdeskapi.exception.ResourceNotFoundException;
import br.com.helpTI.helpdeskapi.repository.CategoriaRepository;
import br.com.helpTI.helpdeskapi.repository.ChamadoRepository;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
import br.com.helpTI.helpdeskapi.repository.SubCategoriaRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository repository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private SubCategoriaRepository subCategoriaRepository;
    @Autowired
    private FileStorageService fileStorageService;

    // -------------------------------------------------------------------------
    // BUSCAS / FINDERS
    // -------------------------------------------------------------------------
    public Chamado findById(Long id) {
        Optional<Chamado> obj = repository.findById(id);
        return obj.orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado! ID: " + id));
    }

    public List<Chamado> findAll() {
        String emailUsuario = getEmailUsuarioLogado();
        System.out.println("🔍 [DEBUG] Solicitante: " + emailUsuario);

        // 1. TÉCNICO
        if (tecnicoRepository.findByEmail(emailUsuario).isPresent()) {
            return filtrarEOrdenar(repository.findAll());
        }

        // 2. EMPRESA (Lógica Corrigida)
        Optional<Empresa> empresaOpt = empresaRepository.findByEmailResponsavel(emailUsuario);
        if (empresaOpt.isPresent()) {
            Empresa empresaLogada = empresaOpt.get();
            System.out.println("🏢 Empresa Logada: " + empresaLogada.getNomeFantasia());

            // --- AQUI ESTÁ A MUDANÇA ---
            // Buscamos as filhas passando o OBJETO, não o ID.
            List<Empresa> empresasDaFamilia = empresaRepository.findByPrestadora(empresaLogada);
            
            // Adiciona a própria empresa na lista (para ver os próprios chamados)
            empresasDaFamilia.add(empresaLogada);

            System.out.println("👪 Família encontrada: " + empresasDaFamilia.size() + " empresas.");
            // ---------------------------

            return filtrarEOrdenar(repository.findAllByEmpresaIn(empresasDaFamilia));
        }

        // 3. CLIENTE / GESTOR
        Optional<Cliente> clienteOpt = clienteRepository.findByEmailIgnoreCase(emailUsuario);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            String perfil = cliente.getPerfil();

            boolean isGestor = perfil != null && (perfil.contains("GESTOR") || perfil.equals("3"));

            if (isGestor) {
                Empresa empresaDoGestor = cliente.getEmpresa();
                
                // Mesma correção para o Gestor
                List<Empresa> empresasDaFamilia = empresaRepository.findByPrestadora(empresaDoGestor);
                empresasDaFamilia.add(empresaDoGestor);
                
                return filtrarEOrdenar(repository.findAllByEmpresaIn(empresasDaFamilia));
            } else {
                return filtrarEOrdenar(repository.findAllByClienteId(cliente.getId()));
            }
        }

        return List.of();
    }
    
    private String getEmailUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private List<Chamado> filtrarEOrdenar(List<Chamado> chamados) {
        return chamados.stream()
            .filter(c -> {
                if (!"FECHADO".equals(c.getStatus())) return true;
                if (c.getDataFechamento() != null) {
                    LocalDateTime agoraMenos24h = LocalDateTime.now().minusHours(24);
                    return c.getDataFechamento().isAfter(agoraMenos24h);
                }
                return false;
            })
            .sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
            .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // CRIAÇÃO / ATUALIZAÇÃO
    // -------------------------------------------------------------------------
    
    @Transactional
    public Chamado create(Chamado obj, List<MultipartFile> anexos) {
        // 1. Valida Cliente
        if (obj.getCliente() == null || obj.getCliente().getId() == null) {
            throw new IllegalArgumentException("ID do Cliente é obrigatório");
        }
        
        // 2. Busca o Cliente no Banco (Fonte da Verdade)
        Cliente cliente = clienteRepository.findById(obj.getCliente().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));

        // 3. Define a Empresa Automaticamente
        Empresa empresaDoCliente = cliente.getEmpresa();
        
        if (empresaDoCliente == null) {
            throw new IllegalStateException("Cliente não possui empresa vinculada! Contate o suporte.");
        }

        // 4. Configura o Chamado
        obj.setId(null); 
        obj.setCliente(cliente);
        obj.setEmpresa(empresaDoCliente);
        
        obj.setDataAbertura(LocalDateTime.now());
        obj.setStatus("ABERTO");
        obj.setStatusPagamento("PENDENTE"); 
        obj.setValorDoChamado(BigDecimal.ZERO);
        obj.setValorPendente(BigDecimal.ZERO);
        
        Chamado novoChamado = repository.save(obj);
        
        // 5. Salva Anexos
        if (anexos != null && !anexos.isEmpty()) {
            for (MultipartFile file : anexos) {
                if (!file.isEmpty()) {
                    String nomeArquivoSalvo = fileStorageService.storeFile(file);

                    Anexo anexo = new Anexo();
                    anexo.setUrlArquivo(nomeArquivoSalvo);
                    anexo.setNomeOriginal(file.getOriginalFilename());
                    anexo.setTipoArquivo(file.getContentType());
                    anexo.setChamado(novoChamado); 

                    novoChamado.getAnexos().add(anexo);
                }
            }
        }
        
        return repository.save(novoChamado);
    }

    @Transactional
    public Chamado atenderChamado(Long chamadoId, Long tecnicoId) {
        Chamado chamado = findById(chamadoId);
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado"));

        chamado.setTecnico(tecnico);
        chamado.setStatus("EM_ATENDIMENTO");
        
        return repository.save(chamado);
    }

    @Transactional
    public Chamado fecharChamado(Long chamadoId, FechamentoChamadoDTO dto) {
        Chamado chamado = findById(chamadoId);
        
        if (dto.getCategoriaId() != null) {
            Categoria cat = categoriaRepository.findById(dto.getCategoriaId()).orElse(null);
            if (cat != null) chamado.setCategoria(cat.getNome());
        }

        BigDecimal valorPorChamado = BigDecimal.ZERO;
        if(chamado.getEmpresa().getValorPorChamado() != null) {
            valorPorChamado = chamado.getEmpresa().getValorPorChamado();
        }
        
        chamado.setStatus("FECHADO");
        chamado.setDataFechamento(LocalDateTime.now());
        chamado.setSolucao(dto.getSolucao());

        chamado.setValorPago(BigDecimal.ZERO); 
        chamado.setValorDoChamado(valorPorChamado);
        chamado.setValorPendente(valorPorChamado); 
        
        return repository.save(chamado);
    }
    
    // --- MÉTODOS DE CONSULTA ADICIONAIS ---

    @Transactional(readOnly = true)
    public List<Chamado> findAllByEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa != null) {
            return repository.findAllByEmpresa(empresa);
        }
        return List.of();
    }
    
    public List<Chamado> findAllPendentesByEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa != null) {
            return repository.findAllByEmpresaAndValorPendenteGreaterThanOrderByDataFechamentoAsc(empresa, BigDecimal.ZERO);
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<Chamado> findAllAtivosByEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + empresaId));
        return repository.findAllByEmpresaAndStatusNot(empresa, "FECHADO"); 
    }
    
    @Transactional(readOnly = true)
    public List<Chamado> findAllAtivosByTecnico(Long tecnicoId) {
        return repository.findAllByTecnicoId(tecnicoId); 
    }

    public List<Chamado> findAllPendentesByTecnico(Long tecnicoId) {
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId).orElse(null);
        if (tecnico != null) {
            return repository.findAllByTecnicoAndValorPendenteGreaterThanOrderByDataFechamentoAsc(tecnico, BigDecimal.ZERO);
        }
        return List.of();
    }

    // --- DASHBOARD ---
    public List<Chamado> getDashboardEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa != null) {
            LocalDateTime dataLimite = LocalDateTime.now().minusHours(24);
            return repository.findChamadosDashboardEmpresa(empresa, dataLimite);
        }
        return List.of();
    }

    public List<Chamado> getDashboardTecnico(Long tecnicoId) {
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId).orElse(null);
        if (tecnico != null) {
            LocalDateTime dataLimite = LocalDateTime.now().minusHours(24);
            return repository.findChamadosDashboardTecnico(tecnico, dataLimite);
        }
        return List.of();
    }
    
    // --- PAGAMENTOS / NOTAS ---
    @Transactional
    public void registrarPagamentoPorTecnico(Long tecnicoId, BigDecimal valorPago) {
        // 1. Validação básica do input
        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }

        // 2. Busca os chamados pendentes
        List<Chamado> pendentes = findAllPendentesByTecnico(tecnicoId);

        // 3. TRAVA: Se não tem chamados na lista
        if (pendentes.isEmpty()) {
            throw new IllegalArgumentException("Este técnico não possui chamados pendentes para pagamento.");
        }

        // 4. Calcula o Total Real da Dívida
        BigDecimal totalDevido = BigDecimal.ZERO;
        for (Chamado c : pendentes) {
            if (c.getValorPendente() != null) {
                totalDevido = totalDevido.add(c.getValorPendente());
            }
        }

        // 5. TRAVA: Se a dívida for Zero
        if (totalDevido.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("O saldo devedor é R$ 0,00. Nada a pagar.");
        }

        // 6. TRAVA: Se tentar pagar mais do que deve
        if (valorPago.compareTo(totalDevido) > 0) {
            throw new IllegalArgumentException("Erro: Você tentou pagar R$ " + valorPago + 
                ", mas o técnico só tem R$ " + totalDevido + " a receber.");
        }

        // 7. Processa o pagamento (Distribui o valor)
        BigDecimal saldoAPagar = valorPago;
        
        for (Chamado chamado : pendentes) {
            if (saldoAPagar.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal valorPendenteChamado = chamado.getValorPendente();
            // Pega o menor valor (ou o que falta do chamado, ou o que sobrou do dinheiro)
            BigDecimal valorAplicado = saldoAPagar.min(valorPendenteChamado);

            // Atualiza valores
            BigDecimal pagoAnterior = (chamado.getValorPago() == null ? BigDecimal.ZERO : chamado.getValorPago());
            chamado.setValorPago(pagoAnterior.add(valorAplicado));
            
            BigDecimal pendenteAnterior = (chamado.getValorPendente() == null ? chamado.getValorDoChamado() : chamado.getValorPendente());
            BigDecimal novoPendente = pendenteAnterior.subtract(valorAplicado);
            chamado.setValorPendente(novoPendente);

            // Atualiza Status (String)
            if (novoPendente.compareTo(BigDecimal.ZERO) == 0) {
                chamado.setStatusPagamento("PAGO");
            } else {
                chamado.setStatusPagamento("PARCIAL");
            }

            // Subtrai do saldo da mão
            saldoAPagar = saldoAPagar.subtract(valorAplicado);
            
            repository.save(chamado);
        }
    }

    
    @Transactional
    public Chamado adicionarNota(Long chamadoId, String texto, String autorNome, String autorTipo) {
        Chamado chamado = findById(chamadoId);
        Nota nota = new Nota();
        nota.setTexto(texto);
        nota.setAutorNome(autorNome);
        nota.setAutorTipo(autorTipo);
        nota.setChamado(chamado);
        chamado.getNotas().add(nota);
        if ("TECNICO".equals(autorTipo) && "ABERTO".equals(chamado.getStatus())) {
            chamado.setStatus("EM_ATENDIMENTO");
        }
        return repository.save(chamado);
    }
    
    @Transactional
    public Chamado trocarTecnico(Long chamadoId, Long novoTecnicoId) {
        Chamado chamado = findById(chamadoId);
        Tecnico novoTecnico = tecnicoRepository.findById(novoTecnicoId).orElse(null);
        if (novoTecnico != null) {
            chamado.setTecnico(novoTecnico);
            adicionarNota(chamadoId, "Chamado transferido para " + novoTecnico.getNome(), "SISTEMA", "ADMIN");
        }
        return repository.save(chamado);
    }
}