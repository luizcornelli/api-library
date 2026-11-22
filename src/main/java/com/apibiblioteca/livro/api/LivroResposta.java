package com.apibiblioteca.livro.api;

import java.util.Comparator;
import java.util.List;

import com.apibiblioteca.autor.api.AutorResposta;
import com.apibiblioteca.livro.dominio.Categoria;
import com.apibiblioteca.livro.dominio.DadosLivro;
import com.apibiblioteca.livro.dominio.Livro;

/** Representação de um livro devolvida pela API, com seus autores. */
public record LivroResposta(Long id, String titulo, String isbn, Integer anoPublicacao, Categoria categoria,
		int quantidadeTotal, int quantidadeDisponivel, List<AutorResposta> autores) {

	/** Converte o livro do domínio para a resposta, com autores ordenados por id. */
	public static LivroResposta de(Livro pLivro) {
		DadosLivro dados = pLivro.getDados();
		return builder().id(pLivro.getId()).titulo(dados.titulo()).isbn(dados.isbn().valor())
				.anoPublicacao(dados.anoPublicacao()).categoria(dados.categoria())
				.quantidadeTotal(pLivro.getEstoque().total()).quantidadeDisponivel(pLivro.getEstoque().disponivel())
				.autores(dados.autores().stream().map(AutorResposta::de).sorted(Comparator.comparing(AutorResposta::id)).toList())
				.build();
	}

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de resposta de livro. */
	public static final class Builder {

		private Long id;
		private String titulo;
		private String isbn;
		private Integer anoPublicacao;
		private Categoria categoria;
		private int quantidadeTotal;
		private int quantidadeDisponivel;
		private List<AutorResposta> autores = List.of();

		/** Define o id. */
		public Builder id(Long pId) {
			this.id = pId;
			return this;
		}

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

		/** Define o total de exemplares. */
		public Builder quantidadeTotal(int pQuantidadeTotal) {
			this.quantidadeTotal = pQuantidadeTotal;
			return this;
		}

		/** Define os exemplares disponíveis. */
		public Builder quantidadeDisponivel(int pQuantidadeDisponivel) {
			this.quantidadeDisponivel = pQuantidadeDisponivel;
			return this;
		}

		/** Define os autores. */
		public Builder autores(List<AutorResposta> pAutores) {
			this.autores = pAutores;
			return this;
		}

		/** Cria o DTO. */
		public LivroResposta build() {
			return new LivroResposta(id, titulo, isbn, anoPublicacao, categoria, quantidadeTotal, quantidadeDisponivel,
					autores);
		}
	}
}
