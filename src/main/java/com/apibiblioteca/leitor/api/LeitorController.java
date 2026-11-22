package com.apibiblioteca.leitor.api;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apibiblioteca.compartilhado.api.ConversorPaginacao;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.leitor.aplicacao.LeitorServico;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Endpoints REST de leitores. */
@RestController
@RequestMapping("/leitores")
@Tag(name = "Leitores")
public class LeitorController {

	private final LeitorServico servico;

	public LeitorController(LeitorServico pServico) {
		this.servico = pServico;
	}

	/** Cadastra um leitor e devolve 201 com Location. */
	@PostMapping
	@Operation(summary = "Cadastra um leitor")
	public ResponseEntity<LeitorResposta> criar(@Valid @RequestBody LeitorRequisicao pRequisicao) {
		LeitorResposta resposta = LeitorResposta.de(servico.criar(pRequisicao.nome(), pRequisicao.email()));
		return ResponseEntity.created(URI.create("/leitores/" + resposta.id())).body(resposta);
	}

	/** Busca um leitor pelo id. */
	@GetMapping("/{id}")
	@Operation(summary = "Busca um leitor pelo id")
	public LeitorResposta buscar(@PathVariable("id") Long pId) {
		return LeitorResposta.de(servico.buscar(pId));
	}

	/** Lista leitores paginados. */
	@GetMapping
	@Operation(summary = "Lista leitores paginados")
	public Pagina<LeitorResposta> listar(@PageableDefault(size = 20) Pageable pPaginacao) {
		return servico.listar(ConversorPaginacao.paraDominio(pPaginacao)).mapear(LeitorResposta::de);
	}
}
