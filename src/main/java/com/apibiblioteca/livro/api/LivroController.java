package com.apibiblioteca.livro.api;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apibiblioteca.compartilhado.api.ConversorPaginacao;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.livro.aplicacao.LivroServico;
import com.apibiblioteca.livro.dominio.FiltroLivro;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Endpoints REST de livros. */
@RestController
@RequestMapping("/livros")
@Tag(name = "Livros")
public class LivroController {

	private final LivroServico servico;

	public LivroController(LivroServico pServico) {
		this.servico = pServico;
	}

	/** Cadastra um livro e devolve 201 com Location. */
	@PostMapping
	@Operation(summary = "Cadastra um livro com seus autores")
	public ResponseEntity<LivroResposta> criar(@Valid @RequestBody LivroRequisicao pRequisicao) {
		LivroResposta resposta = LivroResposta.de(servico.criar(pRequisicao.paraComando(), pRequisicao.quantidadeTotal()));
		return ResponseEntity.created(URI.create("/livros/" + resposta.id())).body(resposta);
	}

	/** Busca um livro pelo id. */
	@GetMapping("/{id}")
	@Operation(summary = "Busca um livro pelo id")
	public LivroResposta buscar(@PathVariable("id") Long pId) {
		return LivroResposta.de(servico.buscar(pId));
	}

	/** Lista livros paginados, com filtros opcionais por trecho do título e por autor. */
	@GetMapping
	@Operation(summary = "Lista livros com autores, filtrando por título e autor")
	public Pagina<LivroResposta> listar(@RequestParam(name = "titulo", required = false) String pTitulo,
			@RequestParam(name = "autorId", required = false) Long pAutorId,
			@PageableDefault(size = 20) Pageable pPaginacao) {
		FiltroLivro filtro = new FiltroLivro(pTitulo, pAutorId);
		return servico.listar(filtro, ConversorPaginacao.paraDominio(pPaginacao)).mapear(LivroResposta::de);
	}

	/** Atualiza dados e autores de um livro. */
	@PutMapping("/{id}")
	@Operation(summary = "Atualiza dados e autores de um livro")
	public LivroResposta atualizar(@PathVariable("id") Long pId,
			@Valid @RequestBody LivroAtualizacaoRequisicao pRequisicao) {
		return LivroResposta.de(servico.atualizar(pId, pRequisicao.paraComando()));
	}

	/** Exclui um livro sem empréstimos. */
	@DeleteMapping("/{id}")
	@Operation(summary = "Exclui um livro sem empréstimos")
	public ResponseEntity<Void> excluir(@PathVariable("id") Long pId) {
		servico.excluir(pId);
		return ResponseEntity.noContent().build();
	}
}
