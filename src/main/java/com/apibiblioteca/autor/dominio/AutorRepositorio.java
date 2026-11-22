package com.apibiblioteca.autor.dominio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;

/** Porta de persistência de autores; a implementação fica na infraestrutura. */
public interface AutorRepositorio {

	/** Grava o autor e devolve a versão persistida (com id). */
	Autor salvar(Autor pAutor);

	/** Busca um autor pelo id. */
	Optional<Autor> buscarPorId(Long pId);

	/** Busca os autores dos ids informados; ids inexistentes são ignorados. */
	List<Autor> buscarPorIds(Collection<Long> pIds);

	/** Lista autores paginados. */
	Pagina<Autor> listar(PaginaRequisicao pPagina);

	/** Indica se o autor está vinculado a algum livro. */
	boolean possuiLivros(Long pId);

	/** Remove o autor. */
	void excluir(Long pId);
}
