package com.apibiblioteca.emprestimo.api;

import jakarta.validation.constraints.NotNull;

/** Pedido de empréstimo: qual livro para qual leitor. */
public record EmprestimoRequisicao(@NotNull Long livroId, @NotNull Long leitorId) {

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de pedido de empréstimo. */
	public static final class Builder {

		private Long livroId;
		private Long leitorId;

		/** Define o livro. */
		public Builder livroId(Long pLivroId) {
			this.livroId = pLivroId;
			return this;
		}

		/** Define o leitor. */
		public Builder leitorId(Long pLeitorId) {
			this.leitorId = pLeitorId;
			return this;
		}

		/** Cria o DTO. */
		public EmprestimoRequisicao build() {
			return new EmprestimoRequisicao(livroId, leitorId);
		}
	}
}
