package com.apibiblioteca.livro.infraestrutura;

import com.apibiblioteca.livro.dominio.Categoria;

/** Colunas simples do livro, agrupadas para o construtor da entidade não ter muitos parâmetros. */
record LivroEntidadeDados(String titulo, String isbn, Integer anoPublicacao, Categoria categoria,
		Integer quantidadeTotal, Integer quantidadeDisponivel) {
}
