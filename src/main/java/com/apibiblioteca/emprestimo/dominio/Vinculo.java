package com.apibiblioteca.emprestimo.dominio;

/** Qual livro foi emprestado para qual leitor. */
public record Vinculo(ReferenciaLivro livro, ReferenciaLeitor leitor) {
}
