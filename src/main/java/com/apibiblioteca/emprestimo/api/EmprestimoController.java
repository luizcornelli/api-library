package com.apibiblioteca.emprestimo.api;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apibiblioteca.compartilhado.api.ConversorPaginacao;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.emprestimo.aplicacao.ConsultarEmprestimos;
import com.apibiblioteca.emprestimo.aplicacao.DevolverEmprestimo;
import com.apibiblioteca.emprestimo.aplicacao.RealizarEmprestimo;
import com.apibiblioteca.emprestimo.aplicacao.ResultadoEmprestimo;
import com.apibiblioteca.emprestimo.dominio.StatusEmprestimo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Endpoints REST de empréstimos. Não há PUT nem DELETE: o histórico não pode ser editado nem
 * apagado (essas chamadas recebem 405).
 */
@RestController
@RequestMapping("/emprestimos")
@Tag(name = "Empréstimos")
public class EmprestimoController {

	private final RealizarEmprestimo realizar;
	private final DevolverEmprestimo devolver;
	private final ConsultarEmprestimos consultar;

	public EmprestimoController(RealizarEmprestimo pRealizar, DevolverEmprestimo pDevolver,
			ConsultarEmprestimos pConsultar) {
		this.realizar = pRealizar;
		this.devolver = pDevolver;
		this.consultar = pConsultar;
	}

	/** Empresta o livro: 201 se criou agora, 200 com o empréstimo existente se o pedido se repetiu. */
	@PostMapping
	@Operation(summary = "Empresta um livro (repetir o pedido devolve o empréstimo ativo existente)")
	public ResponseEntity<EmprestimoResposta> emprestar(@Valid @RequestBody EmprestimoRequisicao pRequisicao) {
		ResultadoEmprestimo resultado = realizar.executar(pRequisicao.livroId(), pRequisicao.leitorId());
		EmprestimoResposta resposta = EmprestimoResposta.de(resultado.emprestimo());
		return resultado.criado() ? ResponseEntity.created(URI.create("/emprestimos/" + resposta.id())).body(resposta)
				: ResponseEntity.ok(resposta);
	}

	/** Busca um empréstimo pelo id. */
	@GetMapping("/{id}")
	@Operation(summary = "Busca um empréstimo pelo id")
	public EmprestimoResposta buscar(@PathVariable("id") Long pId) {
		return EmprestimoResposta.de(consultar.buscar(pId));
	}

	/** Lista empréstimos paginados, com filtro opcional de status (ATIVO ou DEVOLVIDO). */
	@GetMapping
	@Operation(summary = "Lista empréstimos, filtrando por status")
	public Pagina<EmprestimoResposta> listar(@RequestParam(name = "status", required = false) StatusEmprestimo pStatus,
			@PageableDefault(size = 20) Pageable pPaginacao) {
		return consultar.listar(pStatus, ConversorPaginacao.paraDominio(pPaginacao)).mapear(EmprestimoResposta::de);
	}

	/** Registra a devolução; repetir devolve o mesmo resultado sem recalcular a multa. */
	@PatchMapping("/{id}/devolucao")
	@Operation(summary = "Devolve um livro emprestado e calcula a multa por atraso")
	public EmprestimoResposta devolver(@PathVariable("id") Long pId) {
		return EmprestimoResposta.de(devolver.executar(pId));
	}
}
