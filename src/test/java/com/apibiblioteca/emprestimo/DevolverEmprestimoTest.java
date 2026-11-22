package com.apibiblioteca.emprestimo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.emprestimo.aplicacao.DevolverEmprestimo;
import com.apibiblioteca.emprestimo.dominio.CalendarioEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.EmprestimoRepositorio;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLeitor;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLivro;
import com.apibiblioteca.emprestimo.dominio.Situacao;
import com.apibiblioteca.emprestimo.dominio.Vinculo;
import com.apibiblioteca.emprestimo.dominio.multa.MultaCategoriaComum;
import com.apibiblioteca.emprestimo.dominio.multa.MultaCategoriaRara;
import com.apibiblioteca.emprestimo.dominio.multa.SeletorEstrategiaMulta;
import com.apibiblioteca.livro.dominio.Categoria;

@ExtendWith(MockitoExtension.class)
class DevolverEmprestimoTest {

	private static final LocalDate PREVISTA = LocalDate.of(2026, 3, 24);
	private static final ZoneId RECIFE = ZoneId.of("America/Recife");

	@Mock
	private EmprestimoRepositorio emprestimos;

	private DevolverEmprestimo casoDeUso(LocalDate pHoje) {
		Clock relogio = Clock.fixed(pHoje.atStartOfDay(RECIFE).plusHours(12).toInstant(), RECIFE);
		SeletorEstrategiaMulta seletor = new SeletorEstrategiaMulta(List.of(new MultaCategoriaComum(), new MultaCategoriaRara()));
		return new DevolverEmprestimo(emprestimos, seletor, new CalendarioEmprestimo(relogio, Period.ofDays(14)));
	}

	private static Emprestimo ativo(Categoria pCategoria) {
		Vinculo vinculo = new Vinculo(new ReferenciaLivro(1L, "Livro", pCategoria), new ReferenciaLeitor(2L, "Leitor"));
		return new Emprestimo(10L, vinculo, new Situacao(PREVISTA.minusDays(14), PREVISTA, null, null));
	}

	private void devolucaoGravaOQueRecebe() {
		when(emprestimos.registrarDevolucao(any())).thenAnswer(chamada -> chamada.getArgument(0));
	}

	@Test
	void deveDevolverNoPrazoSemMulta() {
		when(emprestimos.buscarPorId(10L)).thenReturn(Optional.of(ativo(Categoria.COMUM)));
		devolucaoGravaOQueRecebe();

		Emprestimo devolvido = casoDeUso(PREVISTA).executar(10L);

		assertThat(devolvido.estaDevolvido()).isTrue();
		assertThat(devolvido.getSituacao().dataDevolucao()).isEqualTo(PREVISTA);
		assertThat(devolvido.getSituacao().multa()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void deveCobrarMultaDeLivroComumPorDiaDeAtraso() {
		when(emprestimos.buscarPorId(10L)).thenReturn(Optional.of(ativo(Categoria.COMUM)));
		devolucaoGravaOQueRecebe();

		Emprestimo devolvido = casoDeUso(PREVISTA.plusDays(4)).executar(10L);

		assertThat(devolvido.getSituacao().multa()).isEqualTo(new BigDecimal("4.00"));
	}

	@Test
	void deveCobrarMultaDeLivroRaroPorDiaDeAtraso() {
		when(emprestimos.buscarPorId(10L)).thenReturn(Optional.of(ativo(Categoria.RARO)));
		devolucaoGravaOQueRecebe();

		Emprestimo devolvido = casoDeUso(PREVISTA.plusDays(4)).executar(10L);

		assertThat(devolvido.getSituacao().multa()).isEqualTo(new BigDecimal("20.00"));
	}

	@Test
	void deveDevolverResultadoOriginalSemRecalcularQuandoJaDevolvido() {
		Emprestimo jaDevolvido = ativo(Categoria.RARO);
		jaDevolvido.devolver(PREVISTA.plusDays(1), new MultaCategoriaRara());
		when(emprestimos.buscarPorId(10L)).thenReturn(Optional.of(jaDevolvido));

		Emprestimo resultado = casoDeUso(PREVISTA.plusDays(20)).executar(10L);

		assertThat(resultado.getSituacao().dataDevolucao()).isEqualTo(PREVISTA.plusDays(1));
		assertThat(resultado.getSituacao().multa()).isEqualTo(new BigDecimal("5.00"));
		verify(emprestimos, never()).registrarDevolucao(any());
	}

	@Test
	void deveFalharComNaoEncontradoParaEmprestimoInexistente() {
		when(emprestimos.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> casoDeUso(PREVISTA).executar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}
}
