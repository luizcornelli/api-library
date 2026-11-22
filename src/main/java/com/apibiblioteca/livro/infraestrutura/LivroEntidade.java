package com.apibiblioteca.livro.infraestrutura;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.apibiblioteca.autor.infraestrutura.AutorEntidade;
import com.apibiblioteca.emprestimo.infraestrutura.EmprestimoEntidade;
import com.apibiblioteca.livro.dominio.Categoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/** Mapeamento JPA da tabela livro e do vínculo N:N com autores. */
@Entity
@Table(name = "livro")
public class LivroEntidade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String titulo;

	@Column(nullable = false, length = 13)
	private String isbn;

	@Column(name = "ano_publicacao")
	private Integer anoPublicacao;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Categoria categoria;

	// Não atualizáveis pelo JPA: estoque só muda por UPDATE atômico, para um save de livro
	// nunca sobrescrever uma baixa feita por empréstimo concorrente.
	@Column(name = "quantidade_total", nullable = false, updatable = false)
	private Integer quantidadeTotal;

	@Column(name = "quantidade_disponivel", nullable = false, updatable = false)
	private Integer quantidadeDisponivel;

	@ManyToMany
	@JoinTable(name = "livro_autor", joinColumns = @JoinColumn(name = "livro_id"),
			inverseJoinColumns = @JoinColumn(name = "autor_id"))
	private Set<AutorEntidade> autores = new HashSet<>();

	@OneToMany(mappedBy = "livro")
	private List<EmprestimoEntidade> emprestimos = new ArrayList<>();

	/** Exigido pelo JPA. */
	protected LivroEntidade() {
	}

	/** Cria a entidade a partir dos valores já convertidos pelo mapeador. */
	LivroEntidade(Long pId, LivroEntidadeDados pDados, Set<AutorEntidade> pAutores) {
		this.id = pId;
		this.titulo = pDados.titulo();
		this.isbn = pDados.isbn();
		this.anoPublicacao = pDados.anoPublicacao();
		this.categoria = pDados.categoria();
		this.quantidadeTotal = pDados.quantidadeTotal();
		this.quantidadeDisponivel = pDados.quantidadeDisponivel();
		this.autores = pAutores;
	}

	public Long getId() {
		return id;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getIsbn() {
		return isbn;
	}

	public Integer getAnoPublicacao() {
		return anoPublicacao;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public Integer getQuantidadeTotal() {
		return quantidadeTotal;
	}

	public Integer getQuantidadeDisponivel() {
		return quantidadeDisponivel;
	}

	public Set<AutorEntidade> getAutores() {
		return autores;
	}
}
