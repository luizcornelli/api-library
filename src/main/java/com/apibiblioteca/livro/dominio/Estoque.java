package com.apibiblioteca.livro.dominio;

/**
 * Exemplares de um livro. A baixa e a devolução de exemplares acontecem de forma atômica na
 * persistência (ver EmprestimoRepositorio), não aqui, para serem seguras contra concorrência.
 */
public record Estoque(int total, int disponivel) {

	/** Estoque inicial: todos os exemplares disponíveis. */
	public static Estoque novo(int pTotal) {
		return new Estoque(pTotal, pTotal);
	}
}
