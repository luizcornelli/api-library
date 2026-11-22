package com.apibiblioteca.livro.dominio;

/** Livro do acervo, com seus dados editáveis e o estoque de exemplares. */
public final class Livro {

	private final Long id;
	private DadosLivro dados;
	private final Estoque estoque;

	/** Cria ou reconstitui um livro; id nulo significa livro ainda não persistido. */
	public Livro(Long pId, DadosLivro pDados, Estoque pEstoque) {
		this.id = pId;
		this.dados = pDados;
		this.estoque = pEstoque;
	}

	/** Substitui os dados editáveis; o estoque não muda por aqui. */
	public void atualizar(DadosLivro pDados) {
		this.dados = pDados;
	}

	public Long getId() {
		return id;
	}

	public DadosLivro getDados() {
		return dados;
	}

	public Estoque getEstoque() {
		return estoque;
	}
}
