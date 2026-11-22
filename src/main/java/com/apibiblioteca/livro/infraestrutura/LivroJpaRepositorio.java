package com.apibiblioteca.livro.infraestrutura;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositório Spring Data de livros. */
public interface LivroJpaRepositorio extends JpaRepository<LivroEntidade, Long> {

	/**
	 * 1º passo da listagem: pagina só os ids. Paginar direto com JOIN FETCH de coleção faria o
	 * Hibernate paginar em memória.
	 */
	@Query(value = """
			select l.id from LivroEntidade l
			where lower(l.titulo) like lower(concat('%', :titulo, '%'))
			and (:autorId is null or exists (
			    select 1 from LivroEntidade l2 join l2.autores a where l2.id = l.id and a.id = :autorId))
			""", countQuery = """
			select count(l) from LivroEntidade l
			where lower(l.titulo) like lower(concat('%', :titulo, '%'))
			and (:autorId is null or exists (
			    select 1 from LivroEntidade l2 join l2.autores a where l2.id = l.id and a.id = :autorId))
			""")
	Page<Long> buscarIds(@Param("titulo") String pTitulo, @Param("autorId") Long pAutorId, Pageable pPaginacao);

	/** 2º passo da listagem: carrega os livros da página com os autores numa única consulta. */
	@Query("select distinct l from LivroEntidade l left join fetch l.autores where l.id in :ids order by l.id")
	List<LivroEntidade> buscarComAutores(@Param("ids") Collection<Long> pIds);

	/** Busca um livro com os autores numa única consulta. */
	@Query("select l from LivroEntidade l left join fetch l.autores where l.id = :id")
	Optional<LivroEntidade> buscarComAutoresPorId(@Param("id") Long pId);

	/** Verifica, pela associação 1:N, se o livro tem algum empréstimo. */
	@Query("select count(e) > 0 from LivroEntidade l join l.emprestimos e where l.id = :id")
	boolean possuiEmprestimos(@Param("id") Long pId);
}
