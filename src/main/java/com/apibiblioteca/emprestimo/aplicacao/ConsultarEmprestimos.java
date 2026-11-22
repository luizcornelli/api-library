package com.apibiblioteca.emprestimo.aplicacao;

import org.springframework.stereotype.Service;

import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.EmprestimoRepositorio;
import com.apibiblioteca.emprestimo.dominio.StatusEmprestimo;

/** Consultas de empréstimo: por id e listagem paginada com filtro de status. */
@Service
public class ConsultarEmprestimos {

	private final EmprestimoRepositorio emprestimos;

	public ConsultarEmprestimos(EmprestimoRepositorio pEmprestimos) {
		this.emprestimos = pEmprestimos;
	}

	/** Busca um empréstimo ou falha com 404. */
	public Emprestimo buscar(Long pId) {
		return emprestimos.buscarPorId(pId).orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado."));
	}

	/** Lista empréstimos; status nulo traz todos. */
	public Pagina<Emprestimo> listar(StatusEmprestimo pStatus, PaginaRequisicao pPagina) {
		return emprestimos.listar(pStatus, pPagina);
	}
}
