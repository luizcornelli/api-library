package com.apibiblioteca.emprestimo.infraestrutura;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.apibiblioteca.leitor.infraestrutura.LeitorEntidade;
import com.apibiblioteca.livro.infraestrutura.LivroEntidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Mapeamento JPA da tabela emprestimo. */
@Entity
@Table(name = "emprestimo")
public class EmprestimoEntidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "livro_id")
	private LivroEntidade livro;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "leitor_id")
	private LeitorEntidade leitor;

	@Column(name = "data_emprestimo", nullable = false)
	private LocalDate dataEmprestimo;

	@Column(name = "data_prevista_devolucao", nullable = false)
	private LocalDate dataPrevistaDevolucao;

	@Column(name = "data_devolucao")
	private LocalDate dataDevolucao;

	@Column(precision = 10, scale = 2)
	private BigDecimal multa;

	/** Exigido pelo JPA. */
	protected EmprestimoEntidade() {
	}

	/** Cria um empréstimo novo (ativo) para o livro e o leitor informados. */
	EmprestimoEntidade(LivroEntidade pLivro, LeitorEntidade pLeitor, EmprestimoEntidadeDatas pDatas) {
		this.livro = pLivro;
		this.leitor = pLeitor;
		this.dataEmprestimo = pDatas.dataEmprestimo();
		this.dataPrevistaDevolucao = pDatas.dataPrevistaDevolucao();
	}

	public Long getId() {
		return id;
	}

	public LivroEntidade getLivro() {
		return livro;
	}

	public LeitorEntidade getLeitor() {
		return leitor;
	}

	public LocalDate getDataEmprestimo() {
		return dataEmprestimo;
	}

	public LocalDate getDataPrevistaDevolucao() {
		return dataPrevistaDevolucao;
	}

	public LocalDate getDataDevolucao() {
		return dataDevolucao;
	}

	public BigDecimal getMulta() {
		return multa;
	}
}
