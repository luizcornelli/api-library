package com.apibiblioteca.emprestimo;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.Period;
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
import com.apibiblioteca.emprestimo.api.EmprestimoController;
import com.apibiblioteca.emprestimo.api.EmprestimoRequisicao;
import com.apibiblioteca.emprestimo.aplicacao.ConsultarEmprestimos;
import com.apibiblioteca.emprestimo.aplicacao.DevolverEmprestimo;
import com.apibiblioteca.emprestimo.aplicacao.RealizarEmprestimo;
import com.apibiblioteca.emprestimo.aplicacao.ResultadoEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLeitor;
import com.apibiblioteca.emprestimo.dominio.ReferenciaLivro;
import com.apibiblioteca.emprestimo.dominio.StatusEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Vinculo;
import com.apibiblioteca.emprestimo.dominio.multa.MultaCategoriaComum;
import com.apibiblioteca.livro.dominio.Categoria;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(EmprestimoController.class)
class EmprestimoControllerTest {

	private static final LocalDate HOJE = LocalDate.of(2026, 3, 10);

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JsonMapper json;

	@MockitoBean
	private RealizarEmprestimo realizar;

	@MockitoBean
	private DevolverEmprestimo devolver;

	@MockitoBean
	private ConsultarEmprestimos consultar;

	private static Emprestimo ativo() {
		Vinculo vinculo = new Vinculo(new ReferenciaLivro(1L, "Livro", Categoria.COMUM), new ReferenciaLeitor(2L, "Leitor"));
		return Emprestimo.novo(vinculo, HOJE, Period.ofDays(14)).comId(10L);
	}

	private String corpo() {
		return json.writeValueAsString(EmprestimoRequisicao.builder().livroId(1L).leitorId(2L).build());
	}

	@Test
	void deveRetornar201ComLocationQuandoEmprestimoECriado() throws Exception {
		when(realizar.executar(1L, 2L)).thenReturn(ResultadoEmprestimo.novo(ativo()));

		mvc.perform(post("/emprestimos").contentType(MediaType.APPLICATION_JSON).content(corpo()))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/emprestimos/10"))
				.andExpect(jsonPath("$.status").value("ATIVO"))
				.andExpect(jsonPath("$.dataPrevistaDevolucao").value("2026-03-24"))
				.andExpect(jsonPath("$.email").doesNotExist());
	}

	@Test
	void deveRetornar200ComEmprestimoExistenteQuandoPedidoSeRepete() throws Exception {
		when(realizar.executar(1L, 2L)).thenReturn(ResultadoEmprestimo.existente(ativo()));

		mvc.perform(post("/emprestimos").contentType(MediaType.APPLICATION_JSON).content(corpo()))
				.andExpect(status().isOk())
				.andExpect(header().doesNotExist("Location"))
				.andExpect(jsonPath("$.id").value(10));
	}

	@Test
	void deveRetornar409QuandoNaoHaExemplarDisponivel() throws Exception {
		when(realizar.executar(1L, 2L)).thenThrow(new ConflitoException("Não há exemplar disponível deste livro."));

		mvc.perform(post("/emprestimos").contentType(MediaType.APPLICATION_JSON).content(corpo()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Não há exemplar disponível deste livro."));
	}

	@Test
	void deveRetornar400QuandoFaltaLivroOuLeitor() throws Exception {
		mvc.perform(post("/emprestimos").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.livroId").exists())
				.andExpect(jsonPath("$.campos.leitorId").exists());
		verifyNoInteractions(realizar);
	}

	@Test
	void deveDevolverERetornarMulta() throws Exception {
		Emprestimo devolvido = ativo();
		devolvido.devolver(HOJE.plusDays(16), new MultaCategoriaComum());
		when(devolver.executar(10L)).thenReturn(devolvido);

		mvc.perform(patch("/emprestimos/10/devolucao"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DEVOLVIDO"))
				.andExpect(jsonPath("$.multa").value(2.00));
	}

	@Test
	void deveFiltrarListagemPorStatus() throws Exception {
		when(consultar.listar(StatusEmprestimo.ATIVO, new PaginaRequisicao(0, 20)))
				.thenReturn(new Pagina<>(List.of(ativo()), 0, 20, 1, 1));

		mvc.perform(get("/emprestimos").param("status", "ATIVO"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.conteudo[0].id").value(10));
	}

	@Test
	void deveRetornar405ParaPutEmEmprestimo() throws Exception {
		mvc.perform(put("/emprestimos/10").contentType(MediaType.APPLICATION_JSON).content(corpo()))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
	}

	@Test
	void deveRetornar405ParaDeleteEmEmprestimo() throws Exception {
		mvc.perform(delete("/emprestimos/10"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
	}
}
