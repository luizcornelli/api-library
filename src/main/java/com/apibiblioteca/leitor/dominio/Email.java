package com.apibiblioteca.leitor.dominio;

import java.util.Locale;

/** E-mail normalizado (sem espaços nas pontas e em minúsculas), para a unicidade não depender de caixa. */
public record Email(String valor) {

	/** Normaliza o valor recebido. */
	public Email {
		valor = valor.strip().toLowerCase(Locale.ROOT);
	}

	/** Não expõe o endereço em logs ou mensagens de erro (proteção de dados). */
	@Override
	public String toString() {
		return "Email[***]";
	}
}
