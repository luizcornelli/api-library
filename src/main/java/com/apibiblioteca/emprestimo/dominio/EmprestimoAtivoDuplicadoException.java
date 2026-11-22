package com.apibiblioteca.emprestimo.dominio;

/**
 * O leitor já tem um empréstimo ativo do mesmo livro (detectado pelo índice único parcial).
 * Não vira erro para o cliente: o caso de uso devolve o empréstimo existente (idempotência).
 */
public class EmprestimoAtivoDuplicadoException extends RuntimeException {

	/** Cria a exceção. */
	public EmprestimoAtivoDuplicadoException() {
		super("Já existe empréstimo ativo deste livro para o leitor.");
	}
}
