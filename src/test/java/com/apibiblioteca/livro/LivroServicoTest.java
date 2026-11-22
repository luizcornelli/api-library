package com.apibiblioteca.livro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.autor.dominio.AutorRepositorio;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.livro.aplicacao.ComandoLivro;
import com.apibiblioteca.livro.aplicacao.LivroServico;
import com.apibiblioteca.livro.dominio.Categoria;
import com.apibiblioteca.livro.dominio.DadosLivro;
import com.apibiblioteca.livro.dominio.Estoque;
import com.apibiblioteca.livro.dominio.Isbn;
import com.apibiblioteca.livro.dominio.Livro;
import com.apibiblioteca.livro.dominio.LivroRepositorio;

@ExtendWith(MockitoExtension.class)
class LivroServicoTest {

	private static final Autor AUTOR = new Autor(1L, "Autor Teste", null);

	@Mock
	private LivroRepositorio livros;

	@Mock
	private AutorRepositorio autores;

	@InjectMocks
	private LivroServico servico;

	private static ComandoLivro comando(Set<Long> pAutorIds) {
		return new ComandoLivro("Livro Teste", "978-0-306-40615-7", 2020, Categoria.COMUM, pAutorIds);
	}

	@Test
	void deveCriarLivroComAutoresEEstoqueTodoDisponivel() {
		when(autores.buscarPorIds(Set.of(1L))).thenReturn(List.of(AUTOR));
		when(livros.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

		Livro criado = servico.criar(comando(Set.of(1L)), 3);

		assertThat(criado.getDados().autores()).containsExactly(AUTOR);
		assertThat(criado.getDados().isbn().valor()).isEqualTo("9780306406157");
		assertThat(criado.getEstoque()).isEqualTo(new Estoque(3, 3));
	}

	@Test
	void deveFalharComNaoEncontradoQuandoAlgumAutorNaoExiste() {
		when(autores.buscarPorIds(Set.of(1L, 2L))).thenReturn(List.of(AUTOR));

		assertThatThrownBy(() -> servico.criar(comando(Set.of(1L, 2L)), 1))
				.isInstanceOf(RecursoNaoEncontradoException.class);
		verify(livros, never()).salvar(any());
	}

	@Test
	void deveAtualizarDadosSemAlterarEstoque() {
		Livro existente = new Livro(5L, new DadosLivro("Antigo", new Isbn("9780134685991"), 2000, Categoria.RARO, Set.of()),
				new Estoque(4, 1));
		when(livros.buscarPorId(5L)).thenReturn(Optional.of(existente));
		when(autores.buscarPorIds(Set.of(1L))).thenReturn(List.of(AUTOR));
		when(livros.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

		Livro atualizado = servico.atualizar(5L, comando(Set.of(1L)));

		assertThat(atualizado.getDados().titulo()).isEqualTo("Livro Teste");
		assertThat(atualizado.getDados().categoria()).isEqualTo(Categoria.COMUM);
		assertThat(atualizado.getEstoque()).isEqualTo(new Estoque(4, 1));
	}

	@Test
	void deveFalharComNaoEncontradoAoAtualizarLivroInexistente() {
		when(livros.buscarPorId(9L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servico.atualizar(9L, comando(Set.of(1L))))
				.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void deveExcluirLivroSemEmprestimos() {
		when(livros.buscarPorId(5L)).thenReturn(Optional.of(livroQualquer()));
		when(livros.possuiEmprestimos(5L)).thenReturn(false);

		servico.excluir(5L);

		verify(livros).excluir(5L);
	}

	@Test
	void deveBloquearExclusaoDeLivroComEmprestimos() {
		when(livros.buscarPorId(5L)).thenReturn(Optional.of(livroQualquer()));
		when(livros.possuiEmprestimos(5L)).thenReturn(true);

		assertThatThrownBy(() -> servico.excluir(5L)).isInstanceOf(ConflitoException.class);
		verify(livros, never()).excluir(5L);
	}

	private static Livro livroQualquer() {
		return new Livro(5L, new DadosLivro("T", new Isbn("9780134685991"), null, Categoria.COMUM, Set.of(AUTOR)),
				Estoque.novo(1));
	}
}
