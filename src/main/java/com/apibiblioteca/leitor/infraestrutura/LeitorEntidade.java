package com.apibiblioteca.leitor.infraestrutura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Mapeamento JPA da tabela leitor. */
@Entity
@Table(name = "leitor")
public class LeitorEntidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	@Column(nullable = false, length = 254)
	private String email;

	/** Exigido pelo JPA. */
	protected LeitorEntidade() {
	}

	/** Cria a entidade com todos os campos. */
	public LeitorEntidade(Long pId, String pNome, String pEmail) {
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

	public String getEmail() {
		return email;
	}
}
