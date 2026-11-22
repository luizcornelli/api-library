package com.apibiblioteca.emprestimo.infraestrutura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositório Spring Data de empréstimos, com as gravações atômicas de estoque e devolução. */
public interface EmprestimoJpaRepositorio extends JpaRepository<EmprestimoEntidade, Long> {

	/**
	 * Baixa um exemplar só se houver disponível, numa única instrução atômica. Devolve 0 se não
	 * havia exemplar: duas requisições pelo último exemplar nunca deixam o estoque negativo.
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update LivroEntidade l set l.quantidadeDisponivel = l.quantidadeDisponivel - 1 "
			+ "where l.id = :livroId and l.quantidadeDisponivel > 0")
	int baixarExemplar(@Param("livroId") Long pLivroId);

	/** Devolve um exemplar ao estoque (a check constraint do banco impede passar do total). */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update LivroEntidade l set l.quantidadeDisponivel = l.quantidadeDisponivel + 1 where l.id = :livroId")
	int devolverExemplar(@Param("livroId") Long pLivroId);

	/**
	 * Marca a devolução só se o empréstimo ainda estiver ativo. Devolve 0 se outra devolução chegou
	 * antes: assim o exemplar volta ao estoque uma única vez.
	 */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update EmprestimoEntidade e set e.dataDevolucao = :data, e.multa = :multa "
			+ "where e.id = :id and e.dataDevolucao is null")
	int marcarDevolvido(@Param("id") Long pId, @Param("data") LocalDate pData, @Param("multa") BigDecimal pMulta);

	/** Busca o empréstimo com livro e leitor numa única consulta. */
	@Query("select e from EmprestimoEntidade e join fetch e.livro join fetch e.leitor where e.id = :id")
	Optional<EmprestimoEntidade> buscarCompletoPorId(@Param("id") Long pId);

	/** Busca o empréstimo ativo do livro para o leitor. */
	@Query("select e from EmprestimoEntidade e join fetch e.livro join fetch e.leitor "
			+ "where e.livro.id = :livroId and e.leitor.id = :leitorId and e.dataDevolucao is null")
	Optional<EmprestimoEntidade> buscarAtivo(@Param("livroId") Long pLivroId, @Param("leitorId") Long pLeitorId);

	/** Lista com livro e leitor (JOIN FETCH de relações para-um pagina no banco, sem N+1). */
	@Query(value = """
			select e from EmprestimoEntidade e join fetch e.livro join fetch e.leitor
			where :status = 'TODOS'
			   or (:status = 'ATIVO' and e.dataDevolucao is null)
			   or (:status = 'DEVOLVIDO' and e.dataDevolucao is not null)
			""", countQuery = """
			select count(e) from EmprestimoEntidade e
			where :status = 'TODOS'
			   or (:status = 'ATIVO' and e.dataDevolucao is null)
			   or (:status = 'DEVOLVIDO' and e.dataDevolucao is not null)
			""")
	Page<EmprestimoEntidade> listar(@Param("status") String pStatus, Pageable pPaginacao);
}
