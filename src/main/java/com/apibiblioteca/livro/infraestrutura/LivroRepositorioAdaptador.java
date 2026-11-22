package com.apibiblioteca.livro.infraestrutura;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.infraestrutura.ConversorPagina;
import com.apibiblioteca.compartilhado.infraestrutura.ViolacaoDeConstraint;
import com.apibiblioteca.livro.dominio.FiltroLivro;
import com.apibiblioteca.livro.dominio.Livro;
import com.apibiblioteca.livro.dominio.LivroRepositorio;

/** Implementa a porta LivroRepositorio com Spring Data JPA. */
@Repository
public class LivroRepositorioAdaptador implements LivroRepositorio {

	private final LivroJpaRepositorio jpa;

	public LivroRepositorioAdaptador(LivroJpaRepositorio pJpa) {
		this.jpa = pJpa;
	}

	/** Grava já sincronizando com o banco, para traduzir ISBN duplicado em 409 aqui mesmo. */
	@Override
	public Livro salvar(Livro pLivro) {
		try {
			return LivroMapeador.paraDominio(jpa.saveAndFlush(LivroMapeador.paraEntidade(pLivro)));
		} catch (DataIntegrityViolationException excecao) {
			throw traduzir(excecao);
		}
	}

	/** Busca o livro com os autores. */
	@Override
	public Optional<Livro> buscarPorId(Long pId) {
		return jpa.buscarComAutoresPorId(pId).map(LivroMapeador::paraDominio);
	}

	/** Lista em 2 consultas fixas (ids da página + livros com autores), sem N+1. */
	@Override
	public Pagina<Livro> listar(FiltroLivro pFiltro, PaginaRequisicao pPagina) {
		Page<Long> ids = jpa.buscarIds(pFiltro.tituloParaBusca(), pFiltro.autorId(), ConversorPagina.paraSpring(pPagina));
		List<LivroEntidade> livros = jpa.buscarComAutores(ids.getContent());
		Page<LivroEntidade> pagina = new PageImpl<>(livros, ids.getPageable(), ids.getTotalElements());
		return ConversorPagina.paraDominio(pagina, LivroMapeador::paraDominio);
	}

	/** Consulta se há empréstimo do livro. */
	@Override
	public boolean possuiEmprestimos(Long pId) {
		return jpa.possuiEmprestimos(pId);
	}

	/** Remove o livro (e seus vínculos com autores). */
	@Override
	public void excluir(Long pId) {
		jpa.deleteById(pId);
	}

	/** ISBN duplicado vira 409 com mensagem de negócio; outras violações seguem para o tratador global. */
	private RuntimeException traduzir(DataIntegrityViolationException pExcecao) {
		if (ViolacaoDeConstraint.violou(pExcecao, "uk_livro_isbn")) {
			return new ConflitoException("Já existe um livro com este ISBN.");
		}
		return pExcecao;
	}
}
