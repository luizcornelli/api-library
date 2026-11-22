package com.apibiblioteca.autor.dominio;

/** Autor de livros. Sem dependência de framework (domínio puro). */
public final class Autor {

	private final Long id;
	private String nome;
	private String nacionalidade;

	/** Cria ou reconstitui um autor; id nulo significa autor ainda não persistido. */
	public Autor(Long pId, String pNome, String pNacionalidade) {
		this.id = pId;
		this.nome = pNome;
		this.nacionalidade = pNacionalidade;
	}

	/** Substitui os dados editáveis do autor, mantendo a identidade. */
	public void atualizar(String pNome, String pNacionalidade) {
		this.nome = pNome;
		this.nacionalidade = pNacionalidade;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getNacionalidade() {
		return nacionalidade;
	}
}
