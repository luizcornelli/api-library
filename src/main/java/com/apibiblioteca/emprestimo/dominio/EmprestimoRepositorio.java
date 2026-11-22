package com.apibiblioteca.emprestimo.dominio;

import java.util.Optional;

import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;

/** Porta de persistência de empréstimos, incluindo a movimentação atômica do estoque. */
public interface EmprestimoRepositorio {

	/** Busca um empréstimo pelo id. */
	Optional<Emprestimo> buscarPorId(Long pId);

	/** Busca o empréstimo ativo do livro para o leitor, se houver. */
	Optional<Emprestimo> buscarAtivo(Long pLivroId, Long pLeitorId);

	/**
	 * Baixa um exemplar e grava o empréstimo na mesma transação.
	 *
	 * @throws com.apibiblioteca.compartilhado.dominio.ConflitoException se não houver exemplar disponível
	 * @throws EmprestimoAtivoDuplicadoException se o leitor já tiver empréstimo ativo do livro
	 */
	Emprestimo registrar(Emprestimo pEmprestimo);

	/**
	 * Grava a devolução só se o empréstimo ainda estiver ativo e, nesse caso, devolve o exemplar.
	 * Devolve o estado gravado (o original, se outra devolução chegou antes).
	 */
	Emprestimo registrarDevolucao(Emprestimo pEmprestimo);

	/** Lista empréstimos paginados; status nulo traz todos. */
	Pagina<Emprestimo> listar(StatusEmprestimo pStatus, PaginaRequisicao pPagina);
}
