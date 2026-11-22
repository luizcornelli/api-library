package com.apibiblioteca.compartilhado;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** CA24: a especificação OpenAPI e o Swagger UI documentam todos os endpoints. */
@AutoConfigureMockMvc
class DocumentacaoIntegracaoTest extends IntegracaoBase {

	@Autowired
	private MockMvc mvc;

	@Test
	void deveDocumentarTodosOsEndpointsNoOpenApi() throws Exception {
		mvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/autores'].post").exists())
				.andExpect(jsonPath("$.paths['/autores/{id}'].delete").exists())
				.andExpect(jsonPath("$.paths['/livros'].get").exists())
				.andExpect(jsonPath("$.paths['/livros/{id}'].put").exists())
				.andExpect(jsonPath("$.paths['/leitores'].post").exists())
				.andExpect(jsonPath("$.paths['/emprestimos'].post").exists())
				.andExpect(jsonPath("$.paths['/emprestimos/{id}/devolucao'].patch").exists())
				.andExpect(jsonPath("$.components.schemas.LivroRequisicao").exists())
				.andExpect(jsonPath("$.components.schemas.EmprestimoResposta").exists());
	}

	@Test
	void deveDisponibilizarSwaggerUi() throws Exception {
		mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
	}
}
