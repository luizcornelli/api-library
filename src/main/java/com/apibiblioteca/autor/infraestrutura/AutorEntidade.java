package com.apibiblioteca.autor.infraestrutura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Mapeamento JPA da tabela autor. Fica na infraestrutura para o domínio não depender de JPA. */
@Entity
@Table(name = "autor")
public class AutorEntidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150)
	private String nome;

	@Column(length = 80)
	private String nacionalidade;

	/** Exigido pelo JPA. */
	protected AutorEntidade() {
	}

	/** Cria a entidade com todos os campos. */
	public AutorEntidade(Long pId, String pNome, String pNacionalidade) {
		this.id = pId;
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
