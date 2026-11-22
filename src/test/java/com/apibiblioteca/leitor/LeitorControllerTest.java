package com.apibiblioteca.leitor;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.leitor.api.LeitorController;
import com.apibiblioteca.leitor.api.LeitorRequisicao;
import com.apibiblioteca.leitor.aplicacao.LeitorServico;
import com.apibiblioteca.leitor.dominio.Email;
import com.apibiblioteca.leitor.dominio.Leitor;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(LeitorController.class)
class LeitorControllerTest {

	private static final String EMAIL = "leitor.ficticio@exemplo.test";

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JsonMapper json;

	@MockitoBean
	private LeitorServico servico;

	@Test
	void deveCriarLeitorERetornar201() throws Exception {
		when(servico.criar("Leitor Ficticio", EMAIL)).thenReturn(new Leitor(4L, "Leitor Ficticio", new Email(EMAIL)));
		LeitorRequisicao requisicao = LeitorRequisicao.builder().nome("Leitor Ficticio").email(EMAIL).build();

		mvc.perform(post("/leitores").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/leitores/4"))
				.andExpect(jsonPath("$.email").value(EMAIL));
	}

	@Test
	void deveRetornar400ParaEmailInvalido() throws Exception {
		LeitorRequisicao requisicao = LeitorRequisicao.builder().nome("Leitor").email("sem-arroba").build();

		mvc.perform(post("/leitores").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.email").exists());
	}

	@Test
	void deveRetornar409SemExporEmailDuplicado() throws Exception {
		when(servico.criar("Leitor", EMAIL)).thenThrow(new ConflitoException("Já existe um leitor com este e-mail."));
		LeitorRequisicao requisicao = LeitorRequisicao.builder().nome("Leitor").email(EMAIL).build();

		mvc.perform(post("/leitores").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isConflict())
				.andExpect(content().string(not(containsString(EMAIL))));
	}

	@Test
	void deveListarLeitoresPaginados() throws Exception {
		when(servico.listar(new PaginaRequisicao(0, 20)))
				.thenReturn(new Pagina<>(List.of(new Leitor(1L, "Leitor", new Email(EMAIL))), 0, 20, 1, 1));

		mvc.perform(get("/leitores"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.conteudo[0].id").value(1));
	}

	@Test
	void deveBuscarLeitorPorId() throws Exception {
		when(servico.buscar(1L)).thenReturn(new Leitor(1L, "Leitor", new Email(EMAIL)));

		mvc.perform(get("/leitores/1")).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Leitor"));
	}
}
