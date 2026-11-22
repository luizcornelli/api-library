package com.apibiblioteca.autor.api;

import com.apibiblioteca.autor.dominio.Autor;

/** Representação de um autor devolvida pela API. */
public record AutorResposta(Long id, String nome, String nacionalidade) {

	/** Converte o autor do domínio para a resposta. */
	public static AutorResposta de(Autor pAutor) {
		return builder().id(pAutor.getId()).nome(pAutor.getNome()).nacionalidade(pAutor.getNacionalidade()).build();
	}

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de resposta de autor. */
	public static final class Builder {

		private Long id;
		private String nome;
		private String nacionalidade;

		/** Define o id. */
		public Builder id(Long pId) {
			this.id = pId;
			return this;
		}

		/** Define o nome. */
		public Builder nome(String pNome) {
			this.nome = pNome;
			return this;
		}

		/** Define a nacionalidade. */
		public Builder nacionalidade(String pNacionalidade) {
			this.nacionalidade = pNacionalidade;
			return this;
		}

		/** Cria o DTO. */
		public AutorResposta build() {
			return new AutorResposta(id, nome, nacionalidade);
		}
	}
}
