package com.apibiblioteca.leitor.infraestrutura;

import org.springframework.data.jpa.repository.JpaRepository;

/** Repositório Spring Data de leitores. */
public interface LeitorJpaRepositorio extends JpaRepository<LeitorEntidade, Long> {
}
