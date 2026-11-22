package com.apibiblioteca.livro.aplicacao;

import java.util.Set;

import com.apibiblioteca.livro.dominio.Categoria;

/** Dados recebidos para criar ou atualizar um livro, com os autores por id. */
public record ComandoLivro(String titulo, String isbn, Integer anoPublicacao, Categoria categoria, Set<Long> autorIds) {
}
