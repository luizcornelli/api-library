package com.apibiblioteca.leitor.infraestrutura;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.infraestrutura.ConversorPagina;
import com.apibiblioteca.compartilhado.infraestrutura.ViolacaoDeConstraint;
import com.apibiblioteca.leitor.dominio.Leitor;
import com.apibiblioteca.leitor.dominio.LeitorRepositorio;

/** Implementa a porta LeitorRepositorio com Spring Data JPA. */
@Repository
public class LeitorRepositorioAdaptador implements LeitorRepositorio {

	private final LeitorJpaRepositorio jpa;

	public LeitorRepositorioAdaptador(LeitorJpaRepositorio pJpa) {
		this.jpa = pJpa;
	}

	/** Grava já sincronizando com o banco, para traduzir e-mail duplicado em 409 aqui mesmo. */
	@Override
	public Leitor salvar(Leitor pLeitor) {
		try {
			return LeitorMapeador.paraDominio(jpa.saveAndFlush(LeitorMapeador.paraEntidade(pLeitor)));
		} catch (DataIntegrityViolationException excecao) {
			throw traduzir(excecao);
		}
	}

	/** Busca por id. */
	@Override
	public Optional<Leitor> buscarPorId(Long pId) {
		return jpa.findById(pId).map(LeitorMapeador::paraDominio);
	}

	/** Lista paginada ordenada por id. */
	@Override
	public Pagina<Leitor> listar(PaginaRequisicao pPagina) {
		return ConversorPagina.paraDominio(jpa.findAll(ConversorPagina.paraSpring(pPagina)), LeitorMapeador::paraDominio);
	}

	/** E-mail duplicado vira 409 sem repetir o endereço na mensagem (proteção de dados). */
	private RuntimeException traduzir(DataIntegrityViolationException pExcecao) {
		if (ViolacaoDeConstraint.violou(pExcecao, "uk_leitor_email")) {
			return new ConflitoException("Já existe um leitor com este e-mail.");
		}
		return pExcecao;
	}
}
