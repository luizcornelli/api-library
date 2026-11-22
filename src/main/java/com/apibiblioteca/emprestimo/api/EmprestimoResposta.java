package com.apibiblioteca.emprestimo.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.Situacao;
import com.apibiblioteca.emprestimo.dominio.StatusEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Vinculo;

/** Representação de um empréstimo devolvida pela API (sem e-mail do leitor). */
public record EmprestimoResposta(Long id, Long livroId, String tituloLivro, Long leitorId, String nomeLeitor,
		LocalDate dataEmprestimo, LocalDate dataPrevistaDevolucao, LocalDate dataDevolucao, BigDecimal multa,
		StatusEmprestimo status) {

	/** Converte o empréstimo do domínio para a resposta. */
	public static EmprestimoResposta de(Emprestimo pEmprestimo) {
		Vinculo vinculo = pEmprestimo.getVinculo();
		Situacao situacao = pEmprestimo.getSituacao();
		return builder().id(pEmprestimo.getId()).livroId(vinculo.livro().id()).tituloLivro(vinculo.livro().titulo())
				.leitorId(vinculo.leitor().id()).nomeLeitor(vinculo.leitor().nome()).situacao(situacao)
				.status(pEmprestimo.status()).build();
	}

	/** Inicia um Builder. */
	public static Builder builder() {
		return new Builder();
	}

	/** Builder do DTO de resposta de empréstimo. */
	public static final class Builder {

		private Long id;
		private Long livroId;
		private String tituloLivro;
		private Long leitorId;
		private String nomeLeitor;
		private Situacao situacao = new Situacao(null, null, null, null);
		private StatusEmprestimo status;

		/** Define o id. */
		public Builder id(Long pId) {
			this.id = pId;
			return this;
		}

		/** Define o livro. */
		public Builder livroId(Long pLivroId) {
			this.livroId = pLivroId;
			return this;
		}

		/** Define o título do livro. */
		public Builder tituloLivro(String pTituloLivro) {
			this.tituloLivro = pTituloLivro;
			return this;
		}

		/** Define o leitor. */
		public Builder leitorId(Long pLeitorId) {
			this.leitorId = pLeitorId;
			return this;
		}

		/** Define o nome do leitor. */
		public Builder nomeLeitor(String pNomeLeitor) {
			this.nomeLeitor = pNomeLeitor;
			return this;
		}

		/** Define datas e multa de uma vez. */
		public Builder situacao(Situacao pSituacao) {
			this.situacao = pSituacao;
			return this;
		}

		/** Define o status. */
		public Builder status(StatusEmprestimo pStatus) {
			this.status = pStatus;
			return this;
		}

		/** Cria o DTO. */
		public EmprestimoResposta build() {
			return new EmprestimoResposta(id, livroId, tituloLivro, leitorId, nomeLeitor, situacao.dataEmprestimo(),
					situacao.dataPrevistaDevolucao(), situacao.dataDevolucao(), situacao.multa(), status);
		}
	}
}
