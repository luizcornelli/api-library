package com.apibiblioteca.autor.aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.autor.dominio.AutorRepositorio;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;

/** Casos de uso de autor: criar, consultar, listar, atualizar e excluir. */
@Service
public class AutorServico {

	private final AutorRepositorio repositorio;

	public AutorServico(AutorRepositorio pRepositorio) {
		this.repositorio = pRepositorio;
	}

	/** Cadastra um autor novo. */
	@Transactional
	public Autor criar(String pNome, String pNacionalidade) {
		return repositorio.salvar(new Autor(null, pNome, pNacionalidade));
	}

	/** Busca um autor ou falha com 404. */
	@Transactional(readOnly = true)
	public Autor buscar(Long pId) {
		return repositorio.buscarPorId(pId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Autor não encontrado."));
	}

	/** Lista autores paginados. */
	@Transactional(readOnly = true)
	public Pagina<Autor> listar(PaginaRequisicao pPagina) {
		return repositorio.listar(pPagina);
	}

	/** Atualiza nome e nacionalidade de um autor existente. */
	@Transactional
	public Autor atualizar(Long pId, String pNome, String pNacionalidade) {
		Autor autor = buscar(pId);
		autor.atualizar(pNome, pNacionalidade);
		return repositorio.salvar(autor);
	}

	/** Exclui o autor; bloqueia com 409 se houver livro vinculado, para não deixar livro órfão. */
	@Transactional
	public void excluir(Long pId) {
		buscar(pId);
		if (repositorio.possuiLivros(pId)) {
			throw new ConflitoException("Autor possui livros vinculados e não pode ser excluído.");
		}
		repositorio.excluir(pId);
	}
}
