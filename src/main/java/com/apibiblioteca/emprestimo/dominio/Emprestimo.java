package com.apibiblioteca.emprestimo.dominio;

import java.time.LocalDate;
import java.time.Period;

import com.apibiblioteca.emprestimo.dominio.multa.EstrategiaMulta;

/** Empréstimo de um livro a um leitor (modelo rico: sabe se devolver e calcular a própria multa). */
public final class Emprestimo {

	private final Long id;
	private final Vinculo vinculo;
	private Situacao situacao;

	/** Reconstitui um empréstimo; id nulo significa empréstimo ainda não persistido. */
	public Emprestimo(Long pId, Vinculo pVinculo, Situacao pSituacao) {
		this.id = pId;
		this.vinculo = pVinculo;
		this.situacao = pSituacao;
	}

	/** Abre um empréstimo hoje, com devolução prevista para hoje + prazo. */
	public static Emprestimo novo(Vinculo pVinculo, LocalDate pHoje, Period pPrazo) {
		return new Emprestimo(null, pVinculo, new Situacao(pHoje, pHoje.plus(pPrazo), null, null));
	}

	/** Cópia com o id gerado pela persistência. */
	public Emprestimo comId(Long pId) {
		return new Emprestimo(pId, vinculo, situacao);
	}

	/** Registra a devolução, calculando a multa pela estratégia da categoria do livro. */
	public void devolver(LocalDate pHoje, EstrategiaMulta pEstrategia) {
		situacao = situacao.devolvidaEm(pHoje, pEstrategia.calcular(situacao.diasDeAtraso(pHoje)));
	}

	/** Indica se o livro já foi devolvido. */
	public boolean estaDevolvido() {
		return situacao.devolvido();
	}

	/** Status derivado das datas. */
	public StatusEmprestimo status() {
		return estaDevolvido() ? StatusEmprestimo.DEVOLVIDO : StatusEmprestimo.ATIVO;
	}

	public Long getId() {
		return id;
	}

	public Vinculo getVinculo() {
		return vinculo;
	}

	public Situacao getSituacao() {
		return situacao;
	}
}
