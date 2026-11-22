package com.apibiblioteca.autor.infraestrutura;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.autor.dominio.AutorRepositorio;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.infraestrutura.ConversorPagina;

/** Implementa a porta AutorRepositorio com Spring Data JPA. */
@Repository
public class AutorRepositorioAdaptador implements AutorRepositorio {

	private final AutorJpaRepositorio jpa;

	public AutorRepositorioAdaptador(AutorJpaRepositorio pJpa) {
		this.jpa = pJpa;
	}

	/** Grava e devolve o autor com id. */
	@Override
	public Autor salvar(Autor pAutor) {
		return AutorMapeador.paraDominio(jpa.save(AutorMapeador.paraEntidade(pAutor)));
	}

	/** Busca por id. */
	@Override
	public Optional<Autor> buscarPorId(Long pId) {
		return jpa.findById(pId).map(AutorMapeador::paraDominio);
	}

	/** Busca vários ids numa só consulta. */
	@Override
	public List<Autor> buscarPorIds(Collection<Long> pIds) {
		return jpa.findAllById(pIds).stream().map(AutorMapeador::paraDominio).toList();
	}

	/** Lista paginada ordenada por id. */
	@Override
	public Pagina<Autor> listar(PaginaRequisicao pPagina) {
		return ConversorPagina.paraDominio(jpa.findAll(ConversorPagina.paraSpring(pPagina)), AutorMapeador::paraDominio);
	}

	/** Consulta o vínculo com livros. */
	@Override
	public boolean possuiLivros(Long pId) {
		return jpa.possuiLivros(pId);
	}

	/** Remove o autor. */
	@Override
	public void excluir(Long pId) {
		jpa.deleteById(pId);
	}
}
