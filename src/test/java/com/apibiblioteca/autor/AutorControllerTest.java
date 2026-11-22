package com.apibiblioteca.autor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.apibiblioteca.autor.api.AutorController;
import com.apibiblioteca.autor.api.AutorRequisicao;
import com.apibiblioteca.autor.aplicacao.AutorServico;
import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AutorController.class)
class AutorControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JsonMapper json;

	@MockitoBean
	private AutorServico servico;

	@Test
	void deveCriarAutorERetornar201ComLocation() throws Exception {
		when(servico.criar("Clarice Lispector", "Brasileira")).thenReturn(new Autor(7L, "Clarice Lispector", "Brasileira"));
		AutorRequisicao requisicao = AutorRequisicao.builder().nome("Clarice Lispector").nacionalidade("Brasileira").build();

		mvc.perform(post("/autores").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/autores/7"))
				.andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.nome").value("Clarice Lispector"));
	}

	@Test
	void deveRetornar400ComCamposInvalidosNoFormatoProblemDetail() throws Exception {
		AutorRequisicao requisicao = AutorRequisicao.builder().nome(" ").build();

		mvc.perform(post("/autores").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.campos.nome").exists());
		verifyNoInteractions(servico);
	}

	@Test
	void deveListarAutoresPaginados() throws Exception {
		when(servico.listar(new PaginaRequisicao(0, 10)))
				.thenReturn(new Pagina<>(List.of(new Autor(1L, "A", null)), 0, 10, 1, 1));

		mvc.perform(get("/autores").param("page", "0").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.conteudo[0].nome").value("A"))
				.andExpect(jsonPath("$.totalElementos").value(1))
				.andExpect(jsonPath("$.tamanho").value(10));
	}

	@Test
	void deveRetornar404NoFormatoProblemDetailParaAutorInexistente() throws Exception {
		when(servico.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Autor não encontrado."));

		mvc.perform(get("/autores/99"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.detail").value("Autor não encontrado."));
	}

	@Test
	void deveAtualizarAutor() throws Exception {
		when(servico.atualizar(1L, "Novo", null)).thenReturn(new Autor(1L, "Novo", null));
		AutorRequisicao requisicao = AutorRequisicao.builder().nome("Novo").build();

		mvc.perform(put("/autores/1").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Novo"));
	}

	@Test
	void deveExcluirAutorERetornar204() throws Exception {
		mvc.perform(delete("/autores/1")).andExpect(status().isNoContent());
	}

	@Test
	void deveRetornar409AoExcluirAutorComLivros() throws Exception {
		doThrow(new ConflitoException("Autor possui livros vinculados e não pode ser excluído.")).when(servico).excluir(1L);

		mvc.perform(delete("/autores/1"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void deveRetornar500GenericoSemStackTrace() throws Exception {
		when(servico.buscar(any())).thenThrow(new IllegalStateException("detalhe interno sigiloso"));

		mvc.perform(get("/autores/1"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.detail").value("Erro interno. Tente novamente mais tarde."))
				.andExpect(content().string(not(containsString("detalhe interno sigiloso"))))
				.andExpect(content().string(not(containsString("IllegalStateException"))))
				.andExpect(content().string(not(containsString("at com.apibiblioteca"))));
	}
}
