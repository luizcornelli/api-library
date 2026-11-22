package com.apibiblioteca.leitor.aplicacao;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.leitor.dominio.Email;
import com.apibiblioteca.leitor.dominio.Leitor;
import com.apibiblioteca.leitor.dominio.LeitorRepositorio;

/** Casos de uso de leitor: cadastrar, consultar e listar. */
@Service
public class LeitorServico {

	private final LeitorRepositorio repositorio;

	public LeitorServico(LeitorRepositorio pRepositorio) {
		this.repositorio = pRepositorio;
	}

	/** Cadastra um leitor; e-mail duplicado vira 409 no repositório. */
	@Transactional
	public Leitor criar(String pNome, String pEmail) {
		return repositorio.salvar(new Leitor(null, pNome, new Email(pEmail)));
	}

	/** Busca um leitor ou falha com 404. */
	@Transactional(readOnly = true)
	public Leitor buscar(Long pId) {
		return repositorio.buscarPorId(pId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado."));
	}

	/** Lista leitores paginados. */
	@Transactional(readOnly = true)
	public Pagina<Leitor> listar(PaginaRequisicao pPagina) {
		return repositorio.listar(pPagina);
	}
}
