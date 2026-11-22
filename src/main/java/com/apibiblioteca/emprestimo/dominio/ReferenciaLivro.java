package com.apibiblioteca.emprestimo.dominio;

import com.apibiblioteca.livro.dominio.Categoria;

/** O que o empréstimo precisa saber do livro: identidade, título e categoria (para a multa). */
public record ReferenciaLivro(Long id, String titulo, Categoria categoria) {
}
