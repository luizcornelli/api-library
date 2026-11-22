package com.apibiblioteca.emprestimo.dominio;

/** O que o empréstimo precisa saber do leitor. Não carrega o e-mail (dado pessoal desnecessário aqui). */
public record ReferenciaLeitor(Long id, String nome) {
}
