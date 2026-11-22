package com.apibiblioteca.compartilhado.dominio;

/** Sinaliza que o recurso pedido não existe; vira HTTP 404 na borda da API. */
public class RecursoNaoEncontradoException extends RuntimeException {

	/** Cria a exceção com uma mensagem segura para exibir ao cliente. */
	public RecursoNaoEncontradoException(String pMensagem) {
		super(pMensagem);
	}
}
