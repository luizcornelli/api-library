package com.apibiblioteca.emprestimo.infraestrutura;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

/** Configuração de empréstimo (biblioteca.emprestimo.*). */
@Validated
@ConfigurationProperties("biblioteca.emprestimo")
public record PropriedadesEmprestimo(@Positive int prazoDias) {
}
