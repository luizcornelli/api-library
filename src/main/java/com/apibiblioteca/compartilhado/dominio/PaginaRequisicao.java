package com.apibiblioteca.compartilhado.dominio;

/**
 * Pedido de página independente de framework, para o domínio não depender do Spring Data.
 *
 * @param numero índice da página, começando em 0
 * @param tamanho quantidade de itens por página
 */
public record PaginaRequisicao(int numero, int tamanho) {
}
