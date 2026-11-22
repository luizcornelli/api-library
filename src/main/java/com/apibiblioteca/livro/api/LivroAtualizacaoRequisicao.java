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

/**
 * Dados de entrada para atualizar um livro. Não tem quantidade de exemplares: o estoque só muda
 * por empréstimo e devolução, de forma atômica.
 */
public record LivroAtualizacaoRequisicao(
		@NotBlank @Size(max = 200) String titulo,
		@NotBlank @ISBN(type = ISBN.Type.ANY) String isbn,
		@Positive @Max(9999) Integer anoPublicacao,
		@NotNull Categoria categoria,
		@NotEmpty Set<@NotNull Long> autorIds) {

	/** Converte para o comando da camada de aplicação. */
	public ComandoLivro paraComando() {
		return new ComandoLivro(titulo, isbn, anoPublicacao, categoria, autorIds);
	}

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de atualização de livro. */
	public static final class Builder {

		private String titulo;
		private String isbn;
		private Integer anoPublicacao;
		private Categoria categoria;
		private Set<Long> autorIds;

		/** Define o título. */
		public Builder titulo(String pTitulo) {
			this.titulo = pTitulo;
			return this;
		}

		/** Define o ISBN. */
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

		/** Define os ids dos autores. */
		public Builder autorIds(Set<Long> pAutorIds) {
			this.autorIds = pAutorIds;
			return this;
		}

		/** Cria o DTO. */
		public LivroAtualizacaoRequisicao build() {
			return new LivroAtualizacaoRequisicao(titulo, isbn, anoPublicacao, categoria, autorIds);
		}
	}
}
