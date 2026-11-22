package com.apibiblioteca.livro.api;

import java.util.Set;

import org.hibernate.validator.constraints.ISBN;

import com.apibiblioteca.livro.aplicacao.ComandoLivro;
import com.apibiblioteca.livro.dominio.Categoria;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados de entrada para cadastrar um livro, incluindo a quantidade de exemplares. */
public record LivroRequisicao(
		@NotBlank @Size(max = 200) String titulo,
		@NotBlank @ISBN(type = ISBN.Type.ANY) String isbn,
		@Positive @Max(9999) Integer anoPublicacao,
		@NotNull Categoria categoria,
		@NotNull @Positive Integer quantidadeTotal,
		@NotEmpty Set<@NotNull Long> autorIds) {

	/** Converte para o comando da camada de aplicação. */
	public ComandoLivro paraComando() {
		return new ComandoLivro(titulo, isbn, anoPublicacao, categoria, autorIds);
	}

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de cadastro de livro. */
	public static final class Builder {

		private String titulo;
		private String isbn;
		private Integer anoPublicacao;
		private Categoria categoria;
		private Integer quantidadeTotal;
		private Set<Long> autorIds;

		/** Define o título. */
		public Builder titulo(String pTitulo) {
			this.titulo = pTitulo;
			return this;
		}

		/** Define o ISBN (10 ou 13 dígitos, com ou sem hífens). */
		public Builder isbn(String pIsbn) {
			this.isbn = pIsbn;
			return this;
		}

		/** Define o ano de publicação. */
		public Builder anoPublicacao(Integer pAnoPublicacao) {
			this.anoPublicacao = pAnoPublicacao;
			return this;
		}

		/** Define a categoria. */
		public Builder categoria(Categoria pCategoria) {
			this.categoria = pCategoria;
			return this;
		}

		/** Define a quantidade de exemplares. */
		public Builder quantidadeTotal(Integer pQuantidadeTotal) {
			this.quantidadeTotal = pQuantidadeTotal;
			return this;
		}

		/** Define os ids dos autores. */
		public Builder autorIds(Set<Long> pAutorIds) {
			this.autorIds = pAutorIds;
			return this;
		}

		/** Cria o DTO. */
		public LivroRequisicao build() {
			return new LivroRequisicao(titulo, isbn, anoPublicacao, categoria, quantidadeTotal, autorIds);
		}
	}
}
