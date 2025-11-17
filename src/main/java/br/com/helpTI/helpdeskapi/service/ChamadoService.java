package br.com.helpTI.helpdeskapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.helpTI.helpdeskapi.domain.Categoria;
import br.com.helpTI.helpdeskapi.domain.Chamado;
import br.com.helpTI.helpdeskapi.domain.Cliente;
import br.com.helpTI.helpdeskapi.domain.Empresa;
import br.com.helpTI.helpdeskapi.domain.SubCategoria;
import br.com.helpTI.helpdeskapi.domain.Tecnico;
import br.com.helpTI.helpdeskapi.dto.FechamentoChamadoDTO;
import br.com.helpTI.helpdeskapi.repository.CategoriaRepository;
import br.com.helpTI.helpdeskapi.repository.ChamadoRepository;
import br.com.helpTI.helpdeskapi.repository.ClienteRepository;
import br.com.helpTI.helpdeskapi.repository.EmpresaRepository;
import br.com.helpTI.helpdeskapi.repository.SubCategoriaRepository;
import br.com.helpTI.helpdeskapi.repository.TecnicoRepository;
import org.springframework.web.multipart.MultipartFile; 
import java.util.List; 
import br.com.helpTI.helpdeskapi.domain.Anexo; 

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

    public Chamado findById(Long id) {
        Optional<Chamado> obj = repository.findById(id);
        return obj.orElse(null); // (Vamos tratar exceções depois)
    }

    public List<Chamado> findAllByEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa != null) {
            return repository.findAllByEmpresa(empresa);
        }
        return List.of(); // Retorna lista vazia
    }

    @Transactional
    public Chamado create(Chamado obj, List<MultipartFile> anexos) {
        Cliente cliente = clienteRepository.findById(obj.getCliente().getId()).orElse(null);
        Empresa empresa = empresaRepository.findById(obj.getEmpresa().getId()).orElse(null);

        obj.setId(null); // Garante que é um novo chamado
        obj.setCliente(cliente);
        obj.setEmpresa(empresa);
        obj.setDataAbertura(LocalDateTime.now());
        obj.setStatus("ABERTO");
        obj.setStatusPagamento("PENDENTE"); // Pagamento sempre pendente ao abrir
        obj.setValorDoChamado(BigDecimal.ZERO);
        obj.setValorPendente(BigDecimal.ZERO);
        
        Chamado novoChamado = repository.save(obj);
        
     // 2. Processa os arquivos
        if (anexos != null && !anexos.isEmpty()) {
            for (MultipartFile file : anexos) {
                if (!file.isEmpty()) {
                    // 3. Salva o arquivo no disco
                    String nomeArquivoSalvo = fileStorageService.storeFile(file);

                    // 4. Cria a entidade Anexo no banco
                    Anexo anexo = new Anexo();
                    anexo.setUrlArquivo(nomeArquivoSalvo); // Salva o nome único
                    anexo.setNomeOriginal(file.getOriginalFilename());
                    anexo.setTipoArquivo(file.getContentType());
                    anexo.setChamado(novoChamado); // Liga o anexo ao chamado

                    // 5. Adiciona o anexo à lista do chamado
                    novoChamado.getAnexos().add(anexo);
                }
            }
        }
        
        return repository.save(novoChamado);
    }

    // --- Atendimento (pelo Técnico) ---
    @Transactional
    public Chamado atenderChamado(Long chamadoId, Long tecnicoId) {
        Chamado chamado = findById(chamadoId);
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId).orElse(null);

        chamado.setTecnico(tecnico);
        chamado.setStatus("EM_ATENDIMENTO");

        // TODO: Enviar e-mail para o cliente avisando que foi atendido
        
        return repository.save(chamado);
    }

    // --- Fechamento (pelo Técnico) ---
    @Transactional
    public Chamado fecharChamado(Long chamadoId, FechamentoChamadoDTO dto) {
        Chamado chamado = findById(chamadoId);
        Categoria cat = categoriaRepository.findById(dto.getCategoriaId()).orElse(null);
        SubCategoria subCat = subCategoriaRepository.findById(dto.getSubCategoriaId()).orElse(null);

        // Pega o valor do chamado das configurações da Empresa
        BigDecimal valorDoChamado = chamado.getEmpresa().getValorPorChamado();
        
        chamado.setStatus("FECHADO");
        chamado.setDataFechamento(LocalDateTime.now());
        chamado.setSolucao(dto.getSolucao());
        chamado.setCategoria(cat);
        chamado.setSubCategoria(subCat);

        // --- AQUI A MÁGICA FINANCEIRA ACONTECE ---
        chamado.setValorDoChamado(valorDoChamado);
        chamado.setValorPendente(valorDoChamado); // A "dívida" é criada

        // TODO: Enviar e-mail para o cliente com a solução
        
        return repository.save(chamado);
    }

    // --- Pagamento (pelo Admin/Dono) ---
    @Transactional
    public void registrarPagamento(Long empresaId, BigDecimal valorPago) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) {
            return; // Ou lançar exceção
        }

        // 1. Busca todos os chamados da empresa com valor pendente, 
        //    do mais antigo para o mais novo (FIFO)
        List<Chamado> pendentes = repository
            .findAllByEmpresaAndValorPendenteGreaterThanOrderByDataFechamentoAsc(
                empresa, BigDecimal.ZERO
            );

        BigDecimal valorDisponivel = valorPago;

        for (Chamado chamado : pendentes) {
            // Se o dinheiro acabou, para de processar
            if (valorDisponivel.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal valorDevido = chamado.getValorPendente();

            if (valorDisponivel.compareTo(valorDevido) >= 0) {
                // O dinheiro disponível PAGA o chamado inteiro
                valorDisponivel = valorDisponivel.subtract(valorDevido);
                chamado.setValorPendente(BigDecimal.ZERO);
                chamado.setStatusPagamento("PAGO");
            } else {
                // O dinheiro disponível PAGA PARCIALMENTE o chamado
                // (Seu caso de 475 pagando um chamado de 50)
                BigDecimal valorRestante = valorDevido.subtract(valorDisponivel);
                chamado.setValorPendente(valorRestante);
                chamado.setStatusPagamento("PARCIAL");
                valorDisponivel = BigDecimal.ZERO; // O dinheiro acabou
            }
            repository.save(chamado); // Salva a atualização do chamado
        }
        
        // Se sobrar dinheiro (valorDisponivel > 0), 
        // poderia ser registrado como crédito, mas para o MVP está ótimo assim.
    }
}