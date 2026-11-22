package com.apibiblioteca.livro.dominio;

/**
 * Filtros opcionais da listagem de livros.
 *
 * @param titulo trecho do título (sem diferença de maiúsculas); nulo não filtra
 * @param autorId id de um autor do livro; nulo não filtra
 */
public record FiltroLivro(String titulo, Long autorId) {

	/** Título pronto para o LIKE: vazio casa com todos os livros. */
	public String tituloParaBusca() {
		return titulo == null ? "" : titulo;
	}
}
