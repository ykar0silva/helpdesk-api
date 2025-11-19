package br.com.helpTI.helpdeskapi.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.helpTI.helpdeskapi.domain.Chamado;
import br.com.helpTI.helpdeskapi.dto.FechamentoChamadoDTO;
import br.com.helpTI.helpdeskapi.service.ChamadoService;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping(value = "/api/chamados")
public class ChamadoController {

	@Autowired
	private ChamadoService service;

	// GET /api/chamados/1
	@GetMapping(value = "/{id}")
	public ResponseEntity<Chamado> findById(@PathVariable Long id) {
		Chamado obj = service.findById(id);
		return ResponseEntity.ok().body(obj);
	}

	// GET /api/chamados/empresa/1 (Busca todos chamados de uma empresa)
	@GetMapping(value = "/empresa/{empresaId}")
	public ResponseEntity<List<Chamado>> findAllByEmpresa(@PathVariable Long empresaId) {
		List<Chamado> list = service.findAllByEmpresa(empresaId);
		return ResponseEntity.ok().body(list);
	}

	// --- ENDPOINTS DE AÇÃO ---

	@PostMapping
	public ResponseEntity<Chamado> create(@RequestParam("chamado") String chamadoJson,
			@RequestParam(value = "anexos", required = false) List<MultipartFile> anexos) {

		Chamado chamado;
		try {
			// 1. Converte o texto JSON (que veio no campo "chamado")
			// de volta para um objeto Java
			ObjectMapper objectMapper = new ObjectMapper();
			chamado = objectMapper.readValue(chamadoJson, Chamado.class);
		} catch (Exception e) {
			// Se o JSON for inválido, retorna um erro
			return ResponseEntity.badRequest().build();
		}

		// 2. Chama o serviço (que agora sabe lidar com arquivos)
		Chamado newObj = service.create(chamado, anexos);
		return ResponseEntity.status(201).body(newObj);
	}

	// PUT /api/chamados/1/atender (Técnico assume o chamado)
	@PutMapping(value = "/{id}/atender")
	public ResponseEntity<Chamado> atenderChamado(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
		// Recebe um JSON simples: { "tecnicoId": 123 }
		Long tecnicoId = payload.get("tecnicoId");
		Chamado obj = service.atenderChamado(id, tecnicoId);
		return ResponseEntity.ok().body(obj);
	}

	// POST /api/chamados/1/notas
	@PostMapping(value = "/{id}/notas")
	public ResponseEntity<Chamado> adicionarNota(@PathVariable Long id, @RequestBody Map<String, String> payload) {

		// O frontend manda: { "texto": "Ola", "autorNome": "Joao", "autorTipo":
		// "TECNICO" }
		String texto = payload.get("texto");
		String autorNome = payload.get("autorNome");
		String autorTipo = payload.get("autorTipo");

		Chamado obj = service.adicionarNota(id, texto, autorNome, autorTipo);
		return ResponseEntity.ok().body(obj);
	}

	// PUT /api/chamados/1/transferir
	@PutMapping(value = "/{id}/transferir")
	public ResponseEntity<Chamado> transferirChamado(@PathVariable Long id, @RequestBody Map<String, Long> payload) {

		Long tecnicoId = payload.get("tecnicoId");
		Chamado obj = service.trocarTecnico(id, tecnicoId);
		return ResponseEntity.ok().body(obj);
	}

	// PUT /api/chamados/1/fechar (Técnico fecha o chamado)
	@PutMapping(value = "/{id}/fechar")
	public ResponseEntity<Chamado> fecharChamado(@PathVariable Long id, @RequestBody FechamentoChamadoDTO dto) {
		Chamado obj = service.fecharChamado(id, dto);
		return ResponseEntity.ok().body(obj);
	}

	// --- ENDPOINT FINANCEIRO (DO ADMIN) ---

	// POST /api/chamados/empresa/1/pagar (Admin registra um pagamento)
	@PostMapping(value = "/empresa/{empresaId}/pagar")
	public ResponseEntity<Void> registrarPagamento(@PathVariable Long empresaId,
			@RequestBody Map<String, BigDecimal> payload) {
		// Recebe um JSON simples: { "valorPago": 475.00 }
		BigDecimal valorPago = payload.get("valorPago");
		service.registrarPagamento(empresaId, valorPago);
		return ResponseEntity.ok().build(); // Retorna 200 OK sem corpo
	}
}