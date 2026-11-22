package com.apibiblioteca.livro.dominio;

/**
 * ISBN normalizado (só dígitos e X maiúsculo), para "978-0-306-40615-7" e "9780306406157"
 * serem o mesmo valor na constraint única.
 */
public record Isbn(String valor) {

	/** Remove hífens, espaços e outros separadores. */
	public Isbn {
		valor = valor.replaceAll("[^0-9Xx]", "").toUpperCase();
	}
}
