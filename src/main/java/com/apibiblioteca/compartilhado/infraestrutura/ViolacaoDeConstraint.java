package com.apibiblioteca.compartilhado.infraestrutura;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

/** Identifica qual constraint do banco foi violada, para traduzir em erro de negócio. */
public final class ViolacaoDeConstraint {

	private ViolacaoDeConstraint() {
	}

	/** Indica se a violação foi da constraint informada (nome sem diferença de maiúsculas). */
	public static boolean violou(DataIntegrityViolationException pExcecao, String pConstraint) {
		return pExcecao.getCause() instanceof ConstraintViolationException violacao
				&& pConstraint.equalsIgnoreCase(violacao.getConstraintName());
	}
}
