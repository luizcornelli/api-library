package com.apibiblioteca.leitor.dominio;

/** Pessoa que pega livros emprestados. Coleta só nome e e-mail (LGPD: o mínimo necessário). */
public final class Leitor {

	private final Long id;
	private final String nome;
	private final Email email;

	/** Cria ou reconstitui um leitor; id nulo significa leitor ainda não persistido. */
	public Leitor(Long pId, String pNome, Email pEmail) {
		this.id = pId;
		this.nome = pNome;
		this.email = pEmail;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public Email getEmail() {
		return email;
	}
}
