package com.apibiblioteca.leitor.api;

import com.apibiblioteca.leitor.dominio.Leitor;

/** Representação de um leitor devolvida pela API. */
public record LeitorResposta(Long id, String nome, String email) {

	/** Converte o leitor do domínio para a resposta. */
	public static LeitorResposta de(Leitor pLeitor) {
		return builder().id(pLeitor.getId()).nome(pLeitor.getNome()).email(pLeitor.getEmail().valor()).build();
	}

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de resposta de leitor. */
	public static final class Builder {

		private Long id;
		private String nome;
		private String email;

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

		/** Define o e-mail. */
		public Builder email(String pEmail) {
			this.email = pEmail;
			return this;
		}

		/** Cria o DTO. */
		public LeitorResposta build() {
			return new LeitorResposta(id, nome, email);
		}
	}
}
