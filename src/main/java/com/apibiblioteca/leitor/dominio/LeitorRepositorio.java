package com.apibiblioteca.leitor.dominio;

import java.util.Optional;

import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;

/** Porta de persistência de leitores. */
public interface LeitorRepositorio {

	/** Grava o leitor; falha com ConflitoException se o e-mail já existir. */
	Leitor salvar(Leitor pLeitor);

	/** Busca um leitor pelo id. */
	Optional<Leitor> buscarPorId(Long pId);

	/** Lista leitores paginados. */
	Pagina<Leitor> listar(PaginaRequisicao pPagina);
}
