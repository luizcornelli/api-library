package com.apibiblioteca.emprestimo.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Datas e multa de um empréstimo. Imutável: a devolução gera uma nova situação.
 *
 * @param dataDevolucao nula enquanto o empréstimo está ativo
 * @param multa nula enquanto o empréstimo está ativo
 */
public record Situacao(LocalDate dataEmprestimo, LocalDate dataPrevistaDevolucao, LocalDate dataDevolucao,
		BigDecimal multa) {

	/** Indica se o livro já foi devolvido. */
	public boolean devolvido() {
		return dataDevolucao != null;
	}

	/** Dias corridos de atraso na data informada; devolver no dia previsto não é atraso. */
	public long diasDeAtraso(LocalDate pData) {
		return Math.max(0, ChronoUnit.DAYS.between(dataPrevistaDevolucao, pData));
	}

	/** Nova situação com a devolução registrada. */
	public Situacao devolvidaEm(LocalDate pData, BigDecimal pMulta) {
		return new Situacao(dataEmprestimo, dataPrevistaDevolucao, pData, pMulta);
	}
}
