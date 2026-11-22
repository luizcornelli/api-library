package com.apibiblioteca.emprestimo.dominio.multa;

import java.math.BigDecimal;

import com.apibiblioteca.livro.dominio.Categoria;

/** Strategy de cálculo de multa por atraso; cada categoria de livro tem a sua. */
public interface EstrategiaMulta {

	/** Categoria de livro atendida por esta estratégia. */
	Categoria categoria();

	/** Valor da multa para os dias de atraso informados (zero se não houver atraso). */
	BigDecimal calcular(long pDiasDeAtraso);
}
