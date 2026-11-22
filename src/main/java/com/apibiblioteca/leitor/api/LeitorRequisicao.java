package com.apibiblioteca.leitor.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados de entrada para cadastrar um leitor. */
public record LeitorRequisicao(
		@NotBlank @Size(max = 150) String nome,
		@NotBlank @Email @Size(max = 254) String email) {

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de cadastro de leitor. */
	public static final class Builder {

		private String nome;
		private String email;

		/** Define o nome. */
		public Builder nome(String pNome) {
			this.nome = pNome;
			return this;
		}

		/** Define o e-mail. */
		public Builder email(String pEmail) {
			this.email = pEmail;
			return this;
		}

		/** Cria o DTO. */
		public LeitorRequisicao build() {
			return new LeitorRequisicao(nome, email);
		}
	}
}
