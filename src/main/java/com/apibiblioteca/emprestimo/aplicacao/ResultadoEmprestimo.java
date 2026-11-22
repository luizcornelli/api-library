package com.apibiblioteca.emprestimo.aplicacao;

import com.apibiblioteca.emprestimo.dominio.Emprestimo;

/**
 * Resultado do pedido de empréstimo.
 *
 * @param criado true se o empréstimo foi criado agora; false se já existia (repetição do pedido)
 */
public record ResultadoEmprestimo(Emprestimo emprestimo, boolean criado) {

	/** Empréstimo criado nesta chamada. */
	public static ResultadoEmprestimo novo(Emprestimo pEmprestimo) {
		return new ResultadoEmprestimo(pEmprestimo, true);
	}

	/** Empréstimo que já existia; a repetição devolve o original em vez de erro. */
	public static ResultadoEmprestimo existente(Emprestimo pEmprestimo) {
		return new ResultadoEmprestimo(pEmprestimo, false);
	}
}
