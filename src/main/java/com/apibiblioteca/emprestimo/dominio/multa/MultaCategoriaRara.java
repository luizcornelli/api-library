package com.apibiblioteca.emprestimo.dominio.multa;

import java.math.BigDecimal;

import com.apibiblioteca.livro.dominio.Categoria;

/** Multa de livro raro: R$ 5,00 por dia de atraso, até 30 dias. */
public class MultaCategoriaRara extends MultaDiariaComTeto {

	/** Atende a categoria RARO. */
	@Override
	public Categoria categoria() {
		return Categoria.RARO;
	}

	/** R$ 5,00 por dia. */
	@Override
	protected BigDecimal valorDiario() {
		return new BigDecimal("5.00");
	}
}
