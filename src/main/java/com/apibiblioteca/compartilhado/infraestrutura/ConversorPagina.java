package com.apibiblioteca.compartilhado.infraestrutura;

import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;

/** Traduz a paginação do domínio para o Spring Data e de volta. */
public final class ConversorPagina {

	private ConversorPagina() {
	}

	/** Cria o pedido do Spring Data, ordenado por id para a paginação ser estável. */
	public static PageRequest paraSpring(PaginaRequisicao pRequisicao) {
		return PageRequest.of(pRequisicao.numero(), pRequisicao.tamanho(), Sort.by("id"));
	}

	/** Converte a página do Spring Data para a do domínio, mapeando cada item. */
	public static <E, D> Pagina<D> paraDominio(Page<E> pPagina, Function<E, D> pConversor) {
		return new Pagina<>(pPagina.getContent().stream().map(pConversor).toList(), pPagina.getNumber(),
				pPagina.getSize(), pPagina.getTotalElements(), pPagina.getTotalPages());
	}
}
