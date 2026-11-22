package com.apibiblioteca.compartilhado.api;

import org.springframework.data.domain.Pageable;

import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;

/** Converte os parâmetros page/size recebidos pela API para o pedido de página do domínio. */
public final class ConversorPaginacao {

	private ConversorPaginacao() {
	}

	/** Usa só número e tamanho; a ordenação é fixa por id para a paginação ser estável. */
	public static PaginaRequisicao paraDominio(Pageable pPaginacao) {
		return new PaginaRequisicao(pPaginacao.getPageNumber(), pPaginacao.getPageSize());
	}
}
