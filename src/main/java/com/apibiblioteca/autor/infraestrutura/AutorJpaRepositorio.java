package com.apibiblioteca.autor.infraestrutura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repositório Spring Data de autores. */
public interface AutorJpaRepositorio extends JpaRepository<AutorEntidade, Long> {

	/** Verifica, pela associação N:N, se algum livro cita o autor. */
	@Query("select count(l) > 0 from LivroEntidade l join l.autores a where a.id = :id")
	boolean possuiLivros(@Param("id") Long pId);
}
