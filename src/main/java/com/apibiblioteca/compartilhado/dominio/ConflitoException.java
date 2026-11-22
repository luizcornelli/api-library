package com.apibiblioteca.compartilhado.dominio;

/** Sinaliza que a operação conflita com o estado atual dos dados; vira HTTP 409 na borda da API. */
public class ConflitoException extends RuntimeException {

	/** Cria a exceção com uma mensagem segura para exibir ao cliente (sem dados pessoais). */
	public ConflitoException(String pMensagem) {
		super(pMensagem);
	}
}
