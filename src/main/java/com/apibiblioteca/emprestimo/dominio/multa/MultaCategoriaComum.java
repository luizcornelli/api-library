package com.apibiblioteca.emprestimo.dominio.multa;

import java.math.BigDecimal;

import com.apibiblioteca.livro.dominio.Categoria;

/** Multa de livro comum: R$ 1,00 por dia de atraso, até 30 dias. */
public class MultaCategoriaComum extends MultaDiariaComTeto {

	/** Atende a categoria COMUM. */
	@Override
	public Categoria categoria() {
		return Categoria.COMUM;
	}

	/** R$ 1,00 por dia. */
	@Override
	protected BigDecimal valorDiario() {
		return new BigDecimal("1.00");
	}
}
