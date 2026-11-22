package com.apibiblioteca.emprestimo.infraestrutura;

import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLeitor;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLivro;
import com.apibiblioteca.emprestimo.dominio.Situacao;
import com.apibiblioteca.emprestimo.dominio.Vinculo;

/** Converte empréstimo entre entidade JPA e domínio. */
final class EmprestimoMapeador {

	private EmprestimoMapeador() {
	}

	/** Entidade → domínio. Acessa livro e leitor: a consulta de origem precisa trazê-los (JOIN FETCH). */
	static Emprestimo paraDominio(EmprestimoEntidade pEntidade) {
		ReferenciaLivro livro = new ReferenciaLivro(pEntidade.getLivro().getId(), pEntidade.getLivro().getTitulo(),
				pEntidade.getLivro().getCategoria());
		ReferenciaLeitor leitor = new ReferenciaLeitor(pEntidade.getLeitor().getId(), pEntidade.getLeitor().getNome());
		Situacao situacao = new Situacao(pEntidade.getDataEmprestimo(), pEntidade.getDataPrevistaDevolucao(),
				pEntidade.getDataDevolucao(), pEntidade.getMulta());
		return new Emprestimo(pEntidade.getId(), new Vinculo(livro, leitor), situacao);
	}
}
