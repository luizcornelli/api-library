package com.apibiblioteca.livro.aplicacao;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.autor.dominio.AutorRepositorio;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.livro.dominio.DadosLivro;
import com.apibiblioteca.livro.dominio.Estoque;
import com.apibiblioteca.livro.dominio.FiltroLivro;
import com.apibiblioteca.livro.dominio.Isbn;
import com.apibiblioteca.livro.dominio.Livro;
import com.apibiblioteca.livro.dominio.LivroRepositorio;

/** Casos de uso de livro: criar, consultar, listar, atualizar e excluir. */
@Service
public class LivroServico {

	private final LivroRepositorio livros;
	private final AutorRepositorio autores;

	public LivroServico(LivroRepositorio pLivros, AutorRepositorio pAutores) {
		this.livros = pLivros;
		this.autores = pAutores;
	}

	/** Cadastra um livro com todos os exemplares disponíveis. */
	@Transactional
	public Livro criar(ComandoLivro pComando, int pQuantidadeTotal) {
		return livros.salvar(new Livro(null, dados(pComando), Estoque.novo(pQuantidadeTotal)));
	}

	/** Busca um livro ou falha com 404. */
	@Transactional(readOnly = true)
	public Livro buscar(Long pId) {
		return livros.buscarPorId(pId).orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado."));
	}

	/** Lista livros filtrados e paginados. */
	@Transactional(readOnly = true)
	public Pagina<Livro> listar(FiltroLivro pFiltro, PaginaRequisicao pPagina) {
		return livros.listar(pFiltro, pPagina);
	}

	/** Atualiza dados e autores; o estoque não é alterado por aqui. */
	@Transactional
	public Livro atualizar(Long pId, ComandoLivro pComando) {
		Livro livro = buscar(pId);
		livro.atualizar(dados(pComando));
		return livros.salvar(livro);
	}

	/** Exclui o livro; bloqueia com 409 se houver empréstimo, para preservar o histórico. */
	@Transactional
	public void excluir(Long pId) {
		buscar(pId);
		if (livros.possuiEmprestimos(pId)) {
			throw new ConflitoException("Livro possui empréstimos e não pode ser excluído.");
		}
		livros.excluir(pId);
	}

	/** Monta os dados do domínio, resolvendo os autores pelos ids. */
	private DadosLivro dados(ComandoLivro pComando) {
		return new DadosLivro(pComando.titulo(), new Isbn(pComando.isbn()), pComando.anoPublicacao(),
				pComando.categoria(), autoresExistentes(pComando.autorIds()));
	}

	/** Carrega os autores e falha com 404 se algum id não existir. */
	private Set<Autor> autoresExistentes(Set<Long> pIds) {
		List<Autor> encontrados = autores.buscarPorIds(pIds);
		if (encontrados.size() != pIds.size()) {
			throw new RecursoNaoEncontradoException("Autor não encontrado.");
		}
		return Set.copyOf(encontrados);
	}
}
