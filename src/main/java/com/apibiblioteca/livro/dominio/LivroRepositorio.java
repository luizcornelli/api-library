package com.apibiblioteca.livro.dominio;

import java.util.Optional;

import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;

/** Porta de persistência de livros. */
public interface LivroRepositorio {

	/** Grava o livro; falha com ConflitoException se o ISBN já existir. */
	Livro salvar(Livro pLivro);

	/** Busca o livro com seus autores. */
	Optional<Livro> buscarPorId(Long pId);

	/** Lista livros com autores, filtrados e paginados, sem N+1. */
	Pagina<Livro> listar(FiltroLivro pFiltro, PaginaRequisicao pPagina);

	/** Indica se o livro tem algum empréstimo (ativo ou histórico). */
	boolean possuiEmprestimos(Long pId);

	/** Remove o livro. */
	void excluir(Long pId);
}
