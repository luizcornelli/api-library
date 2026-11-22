package com.apibiblioteca.emprestimo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.emprestimo.aplicacao.RealizarEmprestimo;
import com.apibiblioteca.emprestimo.aplicacao.ResultadoEmprestimo;
import com.apibiblioteca.emprestimo.dominio.CalendarioEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.EmprestimoAtivoDuplicadoException;
import com.apibiblioteca.emprestimo.dominio.EmprestimoRepositorio;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLeitor;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLivro;
import com.apibiblioteca.emprestimo.dominio.Vinculo;
import com.apibiblioteca.leitor.dominio.Email;
import com.apibiblioteca.leitor.dominio.Leitor;
import com.apibiblioteca.leitor.dominio.LeitorRepositorio;
import com.apibiblioteca.livro.dominio.Categoria;
import com.apibiblioteca.livro.dominio.DadosLivro;
import com.apibiblioteca.livro.dominio.Estoque;
import com.apibiblioteca.livro.dominio.Isbn;
import com.apibiblioteca.livro.dominio.Livro;
import com.apibiblioteca.livro.dominio.LivroRepositorio;

@ExtendWith(MockitoExtension.class)
class RealizarEmprestimoTest {

	// 23:30 em Recife (UTC-3) já é dia 11 em UTC: prova que "hoje" usa o fuso configurado.
	private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-03-11T02:30:00Z"), ZoneId.of("America/Recife"));
	private static final LocalDate HOJE_EM_RECIFE = LocalDate.of(2026, 3, 10);

	@Mock
	private EmprestimoRepositorio emprestimos;

	@Mock
	private LivroRepositorio livros;

	@Mock
	private LeitorRepositorio leitores;

	private RealizarEmprestimo casoDeUso;

	@BeforeEach
	void configurar() {
		casoDeUso = new RealizarEmprestimo(emprestimos, livros, leitores, new CalendarioEmprestimo(RELOGIO, Period.ofDays(14)));
	}

	private void livroELeitorExistem() {
		when(livros.buscarPorId(1L)).thenReturn(Optional.of(new Livro(1L,
				new DadosLivro("Livro", new Isbn("9780306406157"), null, Categoria.COMUM, Set.of()), Estoque.novo(1))));
		when(leitores.buscarPorId(2L)).thenReturn(Optional.of(new Leitor(2L, "Leitor", new Email("l@exemplo.test"))));
	}

	private static Emprestimo ativo(Long pId) {
		Vinculo vinculo = new Vinculo(new ReferenciaLivro(1L, "Livro", Categoria.COMUM), new ReferenciaLeitor(2L, "Leitor"));
		return Emprestimo.novo(vinculo, HOJE_EM_RECIFE, Period.ofDays(14)).comId(pId);
	}

	@Test
	void deveCriarEmprestimoComDevolucaoPrevistaParaHojeMaisPrazoNoFusoDeRecife() {
		livroELeitorExistem();
		when(emprestimos.buscarAtivo(1L, 2L)).thenReturn(Optional.empty());
		when(emprestimos.registrar(any())).thenAnswer(chamada -> chamada.<Emprestimo>getArgument(0).comId(10L));

		ResultadoEmprestimo resultado = casoDeUso.executar(1L, 2L);

		assertThat(resultado.criado()).isTrue();
		assertThat(resultado.emprestimo().getSituacao().dataEmprestimo()).isEqualTo(HOJE_EM_RECIFE);
		assertThat(resultado.emprestimo().getSituacao().dataPrevistaDevolucao()).isEqualTo(LocalDate.of(2026, 3, 24));
		assertThat(resultado.emprestimo().estaDevolvido()).isFalse();
	}

	@Test
	void deveDevolverEmprestimoAtivoExistenteQuandoPedidoSeRepete() {
		when(emprestimos.buscarAtivo(1L, 2L)).thenReturn(Optional.of(ativo(10L)));

		ResultadoEmprestimo resultado = casoDeUso.executar(1L, 2L);

		assertThat(resultado.criado()).isFalse();
		assertThat(resultado.emprestimo().getId()).isEqualTo(10L);
		verify(emprestimos, never()).registrar(any());
	}

	@Test
	void deveDevolverEmprestimoDaRequisicaoConcorrenteQuandoIndiceUnicoBarraDuplicado() {
		livroELeitorExistem();
		when(emprestimos.buscarAtivo(1L, 2L)).thenReturn(Optional.empty(), Optional.of(ativo(10L)));
		when(emprestimos.registrar(any())).thenThrow(new EmprestimoAtivoDuplicadoException());

		ResultadoEmprestimo resultado = casoDeUso.executar(1L, 2L);

		assertThat(resultado.criado()).isFalse();
		assertThat(resultado.emprestimo().getId()).isEqualTo(10L);
	}

	@Test
	void devePropagarConflitoQuandoNaoHaExemplarDisponivel() {
		livroELeitorExistem();
		when(emprestimos.buscarAtivo(1L, 2L)).thenReturn(Optional.empty());
		when(emprestimos.registrar(any())).thenThrow(new ConflitoException("Não há exemplar disponível deste livro."));

		assertThatThrownBy(() -> casoDeUso.executar(1L, 2L)).isInstanceOf(ConflitoException.class);
	}

	@Test
	void deveFalharComNaoEncontradoQuandoLivroNaoExiste() {
		when(emprestimos.buscarAtivo(1L, 2L)).thenReturn(Optional.empty());
		when(livros.buscarPorId(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> casoDeUso.executar(1L, 2L)).isInstanceOf(RecursoNaoEncontradoException.class);
		verify(emprestimos, never()).registrar(any());
	}

	@Test
	void deveFalharComNaoEncontradoQuandoLeitorNaoExiste() {
		when(emprestimos.buscarAtivo(1L, 2L)).thenReturn(Optional.empty());
		when(livros.buscarPorId(1L)).thenReturn(Optional.of(new Livro(1L,
				new DadosLivro("Livro", new Isbn("9780306406157"), null, Categoria.COMUM, Set.of()), Estoque.novo(1))));
		when(leitores.buscarPorId(2L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> casoDeUso.executar(1L, 2L)).isInstanceOf(RecursoNaoEncontradoException.class);
		verify(emprestimos, never()).registrar(any());
	}
}
