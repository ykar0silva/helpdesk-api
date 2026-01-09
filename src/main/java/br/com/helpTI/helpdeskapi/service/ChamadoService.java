package br.com.helpTI.helpdeskapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
        return obj.orElse(null);
    }

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
        tecnicoRepository.findById(tecnicoId) 
            .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado: " + tecnicoId)); 
        
        // Certifique-se que este método existe no seu Repository, se não existir, use findAllByTecnico...
        return repository.findAllByTecnicoId(tecnicoId); // Ajuste conforme o nome no seu Repository
    }

    public List<Chamado> findAllPendentesByTecnico(Long tecnicoId) {
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId).orElse(null);
        if (tecnico != null) {
            return repository.findAllByTecnicoAndValorPendenteGreaterThanOrderByDataFechamentoAsc(tecnico, BigDecimal.ZERO);
        }
        return List.of();
    }
    
    // -------------------------------------------------------------------------
    // CRIAÇÃO / ATUALIZAÇÃO
    // -------------------------------------------------------------------------
    
    @Transactional
    public Chamado create(Chamado obj, List<MultipartFile> anexos) {
        if (obj.getCliente() == null || obj.getCliente().getId() == null) {
            throw new IllegalArgumentException("ID do Cliente é obrigatório");
        }
        Cliente cliente = clienteRepository.findById(obj.getCliente().getId()).orElse(null);
        Empresa empresa = empresaRepository.findById(obj.getEmpresa().getId()).orElse(null);

        obj.setId(null); 
        obj.setCliente(cliente);
        obj.setEmpresa(empresa);
        obj.setDataAbertura(LocalDateTime.now());
        obj.setStatus("ABERTO");
        obj.setStatusPagamento("PENDENTE"); 
        obj.setValorDoChamado(BigDecimal.ZERO);
        obj.setValorPendente(BigDecimal.ZERO);
        
        // OBS: Categoria, Latitude e Longitude já vêm preenchidos no 'obj' pelo Controller
        
        Chamado novoChamado = repository.save(obj);
        
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

    // --- Atendimento ---
    @Transactional
    public Chamado atenderChamado(Long chamadoId, Long tecnicoId) {
        Chamado chamado = findById(chamadoId);
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId).orElse(null);

        chamado.setTecnico(tecnico);
        chamado.setStatus("EM_ATENDIMENTO");
        
        return repository.save(chamado);
    }
    
    @Transactional
    public Chamado fecharChamado(Long chamadoId, FechamentoChamadoDTO dto) {
        Chamado chamado = findById(chamadoId);
        
        // --- CORREÇÃO DO ERRO DA IMAGEM ---
        // Buscamos a Categoria pelo ID que veio do DTO
        Categoria cat = categoriaRepository.findById(dto.getCategoriaId()).orElse(null);
        
        // Agora extraímos apenas o NOME (String) para salvar no chamado
        // (Assumindo que sua classe Categoria tem um método getNome() ou getDescricao())
        if (cat != null) {
            chamado.setCategoria(cat.getNome()); 
        } else {
            // Se não achar categoria, salva null ou uma string padrão
            chamado.setCategoria(null);
        }

        // SubCategoria foi removida do MVP, então comentamos para não dar erro
        // SubCategoria subCat = subCategoriaRepository.findById(dto.getSubCategoriaId()).orElse(null);
        // chamado.setSubCategoria(subCat);

        BigDecimal valorDoChamado = chamado.getEmpresa().getValorPorChamado();
        
        chamado.setStatus("FECHADO");
        chamado.setDataFechamento(LocalDateTime.now());
        chamado.setSolucao(dto.getSolucao());

        chamado.setValorPago(BigDecimal.ZERO); 
        chamado.setValorDoChamado(valorDoChamado);
        chamado.setValorPendente(valorDoChamado); 
        
        return repository.save(chamado);
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
    
    // -------------------------------------------------------------------------
    // PAGAMENTOS
    // -------------------------------------------------------------------------
    
    @Transactional
    public void registrarPagamentoPorTecnico(Long tecnicoId, BigDecimal valorPago) {
        if (valorPago == null || valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }
        
        BigDecimal saldoAPagar = valorPago;
        List<Chamado> pendentes = findAllPendentesByTecnico(tecnicoId);

        for (Chamado chamado : pendentes) {
            if (saldoAPagar.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal valorPendenteChamado = chamado.getValorPendente();
            BigDecimal valorAplicado = saldoAPagar.min(valorPendenteChamado);
            
            chamado.setValorPago(
                (chamado.getValorPago() == null ? BigDecimal.ZERO : chamado.getValorPago())
                .add(valorAplicado)
            );
            chamado.setValorPendente(
                (chamado.getValorPendente() == null ? chamado.getValorDoChamado() : chamado.getValorPendente())
                .subtract(valorAplicado)
            );
            
            saldoAPagar = saldoAPagar.subtract(valorAplicado);
            repository.save(chamado);
        }
    }
    
    @Transactional
    public void registrarPagamento(Long empresaId, BigDecimal valorPago) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) return;

        List<Chamado> pendentes = repository
            .findAllByEmpresaAndValorPendenteGreaterThanOrderByDataFechamentoAsc(
                empresa, BigDecimal.ZERO
            );

        BigDecimal valorDisponivel = valorPago;

        for (Chamado chamado : pendentes) {
            if (valorDisponivel.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal valorDevido = chamado.getValorPendente();

            if (valorDisponivel.compareTo(valorDevido) >= 0) {
                valorDisponivel = valorDisponivel.subtract(valorDevido);
                chamado.setValorPendente(BigDecimal.ZERO);
                chamado.setStatusPagamento("PAGO");
            } else {
                BigDecimal valorRestante = valorDevido.subtract(valorDisponivel);
                chamado.setValorPendente(valorRestante);
                chamado.setStatusPagamento("PARCIAL");
                valorDisponivel = BigDecimal.ZERO; 
            }
            repository.save(chamado); 
        }
    }
    
    // -------------------------------------------------------------------------
    // NOTAS / ANEXOS / TROCAS
    // -------------------------------------------------------------------------
    
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