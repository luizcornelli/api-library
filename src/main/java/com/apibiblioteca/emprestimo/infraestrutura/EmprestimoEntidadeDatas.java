package com.apibiblioteca.emprestimo.infraestrutura;

import java.time.LocalDate;

/** Datas de abertura do empréstimo, agrupadas para o construtor da entidade. */
record EmprestimoEntidadeDatas(LocalDate dataEmprestimo, LocalDate dataPrevistaDevolucao) {
}
