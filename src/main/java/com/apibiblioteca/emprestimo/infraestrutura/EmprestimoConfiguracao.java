package com.apibiblioteca.emprestimo.infraestrutura;

import java.time.Clock;
import java.time.Period;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apibiblioteca.emprestimo.dominio.CalendarioEmprestimo;
import com.apibiblioteca.emprestimo.dominio.multa.EstrategiaMulta;
import com.apibiblioteca.emprestimo.dominio.multa.MultaCategoriaComum;
import com.apibiblioteca.emprestimo.dominio.multa.MultaCategoriaRara;
import com.apibiblioteca.emprestimo.dominio.multa.SeletorEstrategiaMulta;

/** Registra como beans as classes do domínio de empréstimo (que não têm anotação de framework). */
@Configuration
public class EmprestimoConfiguracao {

	/** Estratégia de multa para livros comuns. */
	@Bean
	public MultaCategoriaComum multaCategoriaComum() {
		return new MultaCategoriaComum();
	}

	/** Estratégia de multa para livros raros. */
	@Bean
	public MultaCategoriaRara multaCategoriaRara() {
		return new MultaCategoriaRara();
	}

	/** Seletor com todas as estratégias registradas como bean. */
	@Bean
	public SeletorEstrategiaMulta seletorEstrategiaMulta(List<EstrategiaMulta> pEstrategias) {
		return new SeletorEstrategiaMulta(pEstrategias);
	}

	/** "Hoje" no fuso configurado e prazo de empréstimo vindo da configuração. */
	@Bean
	public CalendarioEmprestimo calendarioEmprestimo(Clock pRelogio, PropriedadesEmprestimo pPropriedades) {
		return new CalendarioEmprestimo(pRelogio, Period.ofDays(pPropriedades.prazoDias()));
	}
}
