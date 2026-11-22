package com.apibiblioteca.emprestimo.aplicacao;

import org.springframework.stereotype.Service;

import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.emprestimo.dominio.CalendarioEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.EmprestimoRepositorio;
import com.apibiblioteca.emprestimo.dominio.multa.SeletorEstrategiaMulta;

/**
 * Caso de uso: devolver um livro. Idempotente: devolver de novo (ou ao mesmo tempo) devolve o
 * resultado original, sem recalcular multa nem devolver o exemplar duas vezes.
 */
@Service
public class DevolverEmprestimo {

	private final EmprestimoRepositorio emprestimos;
	private final SeletorEstrategiaMulta seletor;
	private final CalendarioEmprestimo calendario;

	public DevolverEmprestimo(EmprestimoRepositorio pEmprestimos, SeletorEstrategiaMulta pSeletor,
			CalendarioEmprestimo pCalendario) {
		this.emprestimos = pEmprestimos;
		this.seletor = pSeletor;
		this.calendario = pCalendario;
	}

	/** Registra a devolução com multa, ou devolve o empréstimo como está se já foi devolvido. */
	public Emprestimo executar(Long pId) {
		Emprestimo emprestimo = buscar(pId);
		if (emprestimo.estaDevolvido()) {
			return emprestimo;
		}
		emprestimo.devolver(calendario.hoje(), seletor.para(emprestimo.getVinculo().livro().categoria()));
		return emprestimos.registrarDevolucao(emprestimo);
	}

	/** Busca o empréstimo ou falha com 404. */
	private Emprestimo buscar(Long pId) {
		return emprestimos.buscarPorId(pId).orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado."));
	}
}
