package com.apibiblioteca.emprestimo.dominio;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

/** Fornece "hoje" (no fuso configurado) e o prazo de empréstimo; injetável para fixar a data em testes. */
public record CalendarioEmprestimo(Clock relogio, Period prazo) {

	/** Data de hoje no fuso do relógio. */
	public LocalDate hoje() {
		return LocalDate.now(relogio);
	}
}
