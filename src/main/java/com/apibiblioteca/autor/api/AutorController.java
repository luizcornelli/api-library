package com.apibiblioteca.autor.api;

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
import org.springframework.web.bind.annotation.RestController;

import com.apibiblioteca.autor.aplicacao.AutorServico;
import com.apibiblioteca.compartilhado.api.ConversorPaginacao;
import com.apibiblioteca.compartilhado.dominio.Pagina;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Endpoints REST de autores. */
@RestController
@RequestMapping("/autores")
@Tag(name = "Autores")
public class AutorController {

	private final AutorServico servico;

	public AutorController(AutorServico pServico) {
		this.servico = pServico;
	}

	/** Cria um autor e devolve 201 com Location. */
	@PostMapping
	@Operation(summary = "Cadastra um autor")
	public ResponseEntity<AutorResposta> criar(@Valid @RequestBody AutorRequisicao pRequisicao) {
		AutorResposta resposta = AutorResposta.de(servico.criar(pRequisicao.nome(), pRequisicao.nacionalidade()));
		return ResponseEntity.created(URI.create("/autores/" + resposta.id())).body(resposta);
	}

	/** Busca um autor pelo id. */
	@GetMapping("/{id}")
	@Operation(summary = "Busca um autor pelo id")
	public AutorResposta buscar(@PathVariable("id") Long pId) {
		return AutorResposta.de(servico.buscar(pId));
	}

	/** Lista autores paginados (parâmetros page e size). */
	@GetMapping
	@Operation(summary = "Lista autores paginados")
	public Pagina<AutorResposta> listar(@PageableDefault(size = 20) Pageable pPaginacao) {
		return servico.listar(ConversorPaginacao.paraDominio(pPaginacao)).mapear(AutorResposta::de);
	}

	/** Atualiza um autor. */
	@PutMapping("/{id}")
	@Operation(summary = "Atualiza um autor")
	public AutorResposta atualizar(@PathVariable("id") Long pId, @Valid @RequestBody AutorRequisicao pRequisicao) {
		return AutorResposta.de(servico.atualizar(pId, pRequisicao.nome(), pRequisicao.nacionalidade()));
	}

	/** Exclui um autor sem livros vinculados. */
	@DeleteMapping("/{id}")
	@Operation(summary = "Exclui um autor sem livros vinculados")
	public ResponseEntity<Void> excluir(@PathVariable("id") Long pId) {
		servico.excluir(pId);
		return ResponseEntity.noContent().build();
	}
}
