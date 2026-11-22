package com.apibiblioteca.emprestimo.dominio.multa;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Base das estratégias atuais: valor diário vezes os dias de atraso, limitado a 30 dias. */
public abstract class MultaDiariaComTeto implements EstrategiaMulta {

	static final long TETO_DIAS = 30;

	/** Valor cobrado por dia de atraso. */
	protected abstract BigDecimal valorDiario();

	/** Aplica o valor diário até o teto de dias. */
	@Override
	public BigDecimal calcular(long pDiasDeAtraso) {
		long diasCobrados = Math.min(Math.max(pDiasDeAtraso, 0), TETO_DIAS);
		return valorDiario().multiply(BigDecimal.valueOf(diasCobrados)).setScale(2, RoundingMode.HALF_EVEN);
	}
}
