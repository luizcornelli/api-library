package com.apibiblioteca.livro.dominio;

import java.util.Set;

import com.apibiblioteca.autor.dominio.Autor;

/** Dados editáveis de um livro, agrupados para não passar muitos parâmetros soltos. */
public record DadosLivro(String titulo, Isbn isbn, Integer anoPublicacao, Categoria categoria, Set<Autor> autores) {
}
