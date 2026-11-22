package com.apibiblioteca.autor.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados de entrada para criar ou atualizar um autor. */
public record AutorRequisicao(
		@NotBlank @Size(max = 150) String nome,
		@Size(max = 80) String nacionalidade) {

	/** Inicia um Builder, usado principalmente nos testes e em clientes Java. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de requisição de autor. */
	public static final class Builder {

		private String nome;
		private String nacionalidade;

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
		public AutorRequisicao build() {
			return new AutorRequisicao(nome, nacionalidade);
		}
	}
}
