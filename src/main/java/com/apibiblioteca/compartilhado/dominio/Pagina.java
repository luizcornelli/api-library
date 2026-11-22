package com.apibiblioteca.compartilhado.dominio;

import java.util.List;
import java.util.function.Function;

/**
 * Página de resultados independente de framework.
 *
 * @param conteudo itens da página
 * @param numero índice da página, começando em 0
 * @param tamanho tamanho pedido da página
 * @param totalElementos total de itens em todas as páginas
 * @param totalPaginas total de páginas
 */
public record Pagina<T>(List<T> conteudo, int numero, int tamanho, long totalElementos, int totalPaginas) {

	/** Converte os itens mantendo os metadados, para trocar o tipo sem recalcular a paginação. */
	public <R> Pagina<R> mapear(Function<T, R> pConversor) {
		List<R> convertidos = conteudo.stream().map(pConversor).toList();
		return new Pagina<>(convertidos, numero, tamanho, totalElementos, totalPaginas);
	}
}
