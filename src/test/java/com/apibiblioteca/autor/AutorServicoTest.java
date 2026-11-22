package com.apibiblioteca.autor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apibiblioteca.autor.aplicacao.AutorServico;
import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.autor.dominio.AutorRepositorio;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class AutorServicoTest {

	@Mock
	private AutorRepositorio repositorio;

	@InjectMocks
	private AutorServico servico;

	@Test
	void deveCriarAutorSemIdParaPersistenciaGerar() {
		when(repositorio.salvar(any())).thenAnswer(chamada -> {
			Autor autor = chamada.getArgument(0);
			return new Autor(1L, autor.getNome(), autor.getNacionalidade());
		});

		Autor criado = servico.criar("Machado de Assis", "Brasileira");

		ArgumentCaptor<Autor> enviado = ArgumentCaptor.forClass(Autor.class);
		verify(repositorio).salvar(enviado.capture());
		assertThat(enviado.getValue().getId()).isNull();
		assertThat(criado.getId()).isEqualTo(1L);
		assertThat(criado.getNome()).isEqualTo("Machado de Assis");
	}

	@Test
	void deveFalharComNaoEncontradoAoBuscarAutorInexistente() {
		when(repositorio.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servico.buscar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void deveAtualizarNomeENacionalidadeMantendoId() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(new Autor(1L, "Antigo", "X")));
		when(repositorio.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

		Autor atualizado = servico.atualizar(1L, "Novo", "Portuguesa");

		assertThat(atualizado.getId()).isEqualTo(1L);
		assertThat(atualizado.getNome()).isEqualTo("Novo");
		assertThat(atualizado.getNacionalidade()).isEqualTo("Portuguesa");
	}

	@Test
	void deveExcluirAutorSemLivros() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(new Autor(1L, "A", null)));
		when(repositorio.possuiLivros(1L)).thenReturn(false);

		servico.excluir(1L);

		verify(repositorio).excluir(1L);
	}

	@Test
	void deveBloquearExclusaoDeAutorComLivros() {
		when(repositorio.buscarPorId(1L)).thenReturn(Optional.of(new Autor(1L, "A", null)));
		when(repositorio.possuiLivros(1L)).thenReturn(true);

		assertThatThrownBy(() -> servico.excluir(1L)).isInstanceOf(ConflitoException.class);
		verify(repositorio, never()).excluir(1L);
	}

	@Test
	void deveFalharComNaoEncontradoAoExcluirAutorInexistente() {
		when(repositorio.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servico.excluir(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
		verify(repositorio, never()).excluir(any());
	}
}
