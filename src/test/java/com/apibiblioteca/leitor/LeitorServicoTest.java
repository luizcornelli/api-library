package com.apibiblioteca.leitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.leitor.aplicacao.LeitorServico;
import com.apibiblioteca.leitor.dominio.Leitor;
import com.apibiblioteca.leitor.dominio.LeitorRepositorio;

@ExtendWith(MockitoExtension.class)
class LeitorServicoTest {

	@Mock
	private LeitorRepositorio repositorio;

	@InjectMocks
	private LeitorServico servico;

	@Test
	void deveCriarLeitorComEmailNormalizado() {
		when(repositorio.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

		Leitor criado = servico.criar("Leitora Ficticia", "  Leitora.Ficticia@Exemplo.Test ");

		assertThat(criado.getEmail().valor()).isEqualTo("leitora.ficticia@exemplo.test");
	}

	@Test
	void deveOcultarEmailNoToString() {
		when(repositorio.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

		Leitor criado = servico.criar("Leitora Ficticia", "leitora@exemplo.test");

		assertThat(criado.getEmail().toString()).doesNotContain("leitora@exemplo.test");
	}

	@Test
	void deveFalharComNaoEncontradoAoBuscarLeitorInexistente() {
		when(repositorio.buscarPorId(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servico.buscar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}
}
