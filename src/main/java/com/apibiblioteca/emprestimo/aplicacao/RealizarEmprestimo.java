package com.apibiblioteca.emprestimo.aplicacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.emprestimo.dominio.CalendarioEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.EmprestimoAtivoDuplicadoException;
import com.apibiblioteca.emprestimo.dominio.EmprestimoRepositorio;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLeitor;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLivro;
import com.apibiblioteca.emprestimo.dominio.Vinculo;
import com.apibiblioteca.leitor.dominio.Leitor;
import com.apibiblioteca.leitor.dominio.LeitorRepositorio;
import com.apibiblioteca.livro.dominio.Livro;
import com.apibiblioteca.livro.dominio.LivroRepositorio;

/**
 * Caso de uso: emprestar um livro a um leitor. Idempotente: se o leitor já tem empréstimo ativo
 * do livro (inclusive criado por uma requisição simultânea), devolve esse empréstimo.
 * Sem @Transactional aqui: a gravação tem a própria transação no repositório, e a releitura após
 * um conflito precisa acontecer depois do rollback dela.
 */
@Service
public class RealizarEmprestimo {

	private static final Logger LOG = LoggerFactory.getLogger(RealizarEmprestimo.class);

	private final EmprestimoRepositorio emprestimos;
	private final LivroRepositorio livros;
	private final LeitorRepositorio leitores;
	private final CalendarioEmprestimo calendario;

	public RealizarEmprestimo(EmprestimoRepositorio pEmprestimos, LivroRepositorio pLivros,
			LeitorRepositorio pLeitores, CalendarioEmprestimo pCalendario) {
		this.emprestimos = pEmprestimos;
		this.livros = pLivros;
		this.leitores = pLeitores;
		this.calendario = pCalendario;
	}

	/** Empresta o livro ou devolve o empréstimo ativo já existente. */
	public ResultadoEmprestimo executar(Long pLivroId, Long pLeitorId) {
		return emprestimos.buscarAtivo(pLivroId, pLeitorId).map(ResultadoEmprestimo::existente)
				.orElseGet(() -> registrarNovo(pLivroId, pLeitorId));
	}

	/** Grava o empréstimo; se uma requisição simultânea igual venceu, devolve o dela. */
	private ResultadoEmprestimo registrarNovo(Long pLivroId, Long pLeitorId) {
		Emprestimo novo = Emprestimo.novo(vinculo(pLivroId, pLeitorId), calendario.hoje(), calendario.prazo());
		try {
			return ResultadoEmprestimo.novo(emprestimos.registrar(novo));
		} catch (EmprestimoAtivoDuplicadoException excecao) {
			LOG.debug("Empréstimo simultâneo detectado para o livro {}; devolvendo o existente", pLivroId);
			return ResultadoEmprestimo.existente(ativoExistente(pLivroId, pLeitorId));
		}
	}

	/** Monta o vínculo garantindo que livro e leitor existem (404 se não). */
	private Vinculo vinculo(Long pLivroId, Long pLeitorId) {
		return new Vinculo(referenciaLivro(pLivroId), referenciaLeitor(pLeitorId));
	}

	/** Carrega o livro e extrai o que o empréstimo precisa. */
	private ReferenciaLivro referenciaLivro(Long pLivroId) {
		Livro livro = livros.buscarPorId(pLivroId).orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));
		return new ReferenciaLivro(livro.getId(), livro.getDados().titulo(), livro.getDados().categoria());
	}

	/** Carrega o leitor e extrai o que o empréstimo precisa. */
	private ReferenciaLeitor referenciaLeitor(Long pLeitorId) {
		Leitor leitor = leitores.buscarPorId(pLeitorId).orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado."));
		return new ReferenciaLeitor(leitor.getId(), leitor.getNome());
	}

	/** Relê o empréstimo ativo gravado pela requisição concorrente. */
	private Emprestimo ativoExistente(Long pLivroId, Long pLeitorId) {
		return emprestimos.buscarAtivo(pLivroId, pLeitorId)
				.orElseThrow(() -> new IllegalStateException("Empréstimo ativo duplicado sem registro visível"));
	}
}
