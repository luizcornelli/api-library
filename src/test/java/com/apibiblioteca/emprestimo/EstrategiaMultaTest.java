package com.apibiblioteca.emprestimo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.apibiblioteca.emprestimo.dominio.multa.MultaCategoriaComum;
import com.apibiblioteca.emprestimo.dominio.multa.MultaCategoriaRara;
import com.apibiblioteca.emprestimo.dominio.multa.SeletorEstrategiaMulta;
import com.apibiblioteca.livro.dominio.Categoria;

class EstrategiaMultaTest {

	private final MultaCategoriaComum comum = new MultaCategoriaComum();
	private final MultaCategoriaRara rara = new MultaCategoriaRara();

	@Test
	void deveCobrarZeroSemAtraso() {
		assertThat(comum.calcular(0)).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(rara.calcular(0)).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void deveCobrarUmRealPorDiaParaLivroComum() {
		assertThat(comum.calcular(3)).isEqualTo(new BigDecimal("3.00"));
	}

	@Test
	void deveCobrarCincoReaisPorDiaParaLivroRaro() {
		assertThat(rara.calcular(3)).isEqualTo(new BigDecimal("15.00"));
	}

	@Test
	void deveLimitarMultaATrintaDias() {
		assertThat(comum.calcular(30)).isEqualTo(new BigDecimal("30.00"));
		assertThat(comum.calcular(45)).isEqualTo(new BigDecimal("30.00"));
		assertThat(rara.calcular(45)).isEqualTo(new BigDecimal("150.00"));
	}

	@Test
	void deveSelecionarEstrategiaPelaCategoria() {
		SeletorEstrategiaMulta seletor = new SeletorEstrategiaMulta(List.of(comum, rara));

		assertThat(seletor.para(Categoria.COMUM)).isSameAs(comum);
		assertThat(seletor.para(Categoria.RARO)).isSameAs(rara);
	}

	@Test
	void deveFalharQuandoCategoriaNaoTemEstrategia() {
		SeletorEstrategiaMulta seletor = new SeletorEstrategiaMulta(List.of(comum));

		assertThatThrownBy(() -> seletor.para(Categoria.RARO)).isInstanceOf(IllegalStateException.class);
	}
}
