package com.apibiblioteca.emprestimo.infraestrutura;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.infraestrutura.ConversorPagina;
import com.apibiblioteca.compartilhado.infraestrutura.ViolacaoDeConstraint;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.EmprestimoAtivoDuplicadoException;
import com.apibiblioteca.emprestimo.dominio.EmprestimoRepositorio;
import com.apibiblioteca.emprestimo.dominio.Situacao;
import com.apibiblioteca.emprestimo.dominio.StatusEmprestimo;
import com.apibiblioteca.leitor.infraestrutura.LeitorEntidade;
import com.apibiblioteca.livro.infraestrutura.LivroEntidade;

import jakarta.persistence.EntityManager;

/** Implementa a porta EmprestimoRepositorio; as regras de concorrência ficam no banco. */
@Repository
public class EmprestimoRepositorioAdaptador implements EmprestimoRepositorio {

	private final EmprestimoJpaRepositorio jpa;
	private final EntityManager entityManager;

	public EmprestimoRepositorioAdaptador(EmprestimoJpaRepositorio pJpa, EntityManager pEntityManager) {
		this.jpa = pJpa;
		this.entityManager = pEntityManager;
	}

	/** Busca com livro e leitor. */
	@Override
	@Transactional(readOnly = true)
	public Optional<Emprestimo> buscarPorId(Long pId) {
		return jpa.buscarCompletoPorId(pId).map(EmprestimoMapeador::paraDominio);
	}

	/** Busca o ativo do par livro/leitor. */
	@Override
	@Transactional(readOnly = true)
	public Optional<Emprestimo> buscarAtivo(Long pLivroId, Long pLeitorId) {
		return jpa.buscarAtivo(pLivroId, pLeitorId).map(EmprestimoMapeador::paraDominio);
	}

	/** Baixa o exemplar e grava; qualquer falha desfaz as duas coisas (mesma transação). */
	@Override
	@Transactional
	public Emprestimo registrar(Emprestimo pEmprestimo) {
		if (jpa.baixarExemplar(pEmprestimo.getVinculo().livro().id()) == 0) {
			throw new ConflitoException("Não há exemplar disponível deste livro.");
		}
		return pEmprestimo.comId(inserir(pEmprestimo));
	}

	/** Grava a devolução condicionalmente e devolve o estado que ficou gravado. */
	@Override
	@Transactional
	public Emprestimo registrarDevolucao(Emprestimo pEmprestimo) {
		Situacao situacao = pEmprestimo.getSituacao();
		if (jpa.marcarDevolvido(pEmprestimo.getId(), situacao.dataDevolucao(), situacao.multa()) == 1) {
			jpa.devolverExemplar(pEmprestimo.getVinculo().livro().id());
		}
		return buscarPorId(pEmprestimo.getId()).orElseThrow();
	}

	/** Lista paginada; status nulo traz todos. */
	@Override
	@Transactional(readOnly = true)
	public Pagina<Emprestimo> listar(StatusEmprestimo pStatus, PaginaRequisicao pPagina) {
		String status = pStatus == null ? "TODOS" : pStatus.name();
		return ConversorPagina.paraDominio(jpa.listar(status, ConversorPagina.paraSpring(pPagina)), EmprestimoMapeador::paraDominio);
	}

	/** Insere a linha; empréstimo ativo duplicado (índice único parcial) vira exceção de domínio. */
	private Long inserir(Emprestimo pEmprestimo) {
		try {
			return jpa.saveAndFlush(novaEntidade(pEmprestimo)).getId();
		} catch (DataIntegrityViolationException excecao) {
			throw traduzir(excecao);
		}
	}

	/** Monta a entidade usando referências (sem consultar livro e leitor de novo). */
	private EmprestimoEntidade novaEntidade(Emprestimo pEmprestimo) {
		LivroEntidade livro = entityManager.getReference(LivroEntidade.class, pEmprestimo.getVinculo().livro().id());
		LeitorEntidade leitor = entityManager.getReference(LeitorEntidade.class, pEmprestimo.getVinculo().leitor().id());
		Situacao situacao = pEmprestimo.getSituacao();
		return new EmprestimoEntidade(livro, leitor,
				new EmprestimoEntidadeDatas(situacao.dataEmprestimo(), situacao.dataPrevistaDevolucao()));
	}

	/** Só o índice de empréstimo ativo é traduzido; outras violações seguem para o tratador global. */
	private RuntimeException traduzir(DataIntegrityViolationException pExcecao) {
		if (ViolacaoDeConstraint.violou(pExcecao, "ux_emprestimo_ativo")) {
			return new EmprestimoAtivoDuplicadoException();
		}
		return pExcecao;
	}
}
