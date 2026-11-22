package com.apibiblioteca.livro;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;
import com.apibiblioteca.livro.api.LivroAtualizacaoRequisicao;
import com.apibiblioteca.livro.api.LivroController;
import com.apibiblioteca.livro.api.LivroRequisicao;
import com.apibiblioteca.livro.aplicacao.ComandoLivro;
import com.apibiblioteca.livro.aplicacao.LivroServico;
import com.apibiblioteca.livro.dominio.Categoria;
import com.apibiblioteca.livro.dominio.DadosLivro;
import com.apibiblioteca.livro.dominio.Estoque;
import com.apibiblioteca.livro.dominio.FiltroLivro;
import com.apibiblioteca.livro.dominio.Isbn;
import com.apibiblioteca.livro.dominio.Livro;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(LivroController.class)
class LivroControllerTest {

	private static final Livro LIVRO = new Livro(3L,
			new DadosLivro("Dom Casmurro", new Isbn("9780306406157"), 1899, Categoria.RARO,
					Set.of(new Autor(1L, "Machado de Assis", "Brasileira"))),
			new Estoque(2, 2));

	@Autowired
	private MockMvc mvc;

	@Autowired
	private JsonMapper json;

	@MockitoBean
	private LivroServico servico;

	private static LivroRequisicao.Builder requisicaoValida() {
		return LivroRequisicao.builder().titulo("Dom Casmurro").isbn("978-0-306-40615-7").anoPublicacao(1899)
				.categoria(Categoria.RARO).quantidadeTotal(2).autorIds(Set.of(1L));
	}

	@Test
	void deveCriarLivroComAutoresERetornar201() throws Exception {
		when(servico.criar(any(ComandoLivro.class), eq(2))).thenReturn(LIVRO);

		mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(requisicaoValida().build())))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/livros/3"))
				.andExpect(jsonPath("$.autores[0].nome").value("Machado de Assis"))
				.andExpect(jsonPath("$.quantidadeDisponivel").value(2));
	}

	@Test
	void deveRetornar400ParaIsbnInvalidoESemAutores() throws Exception {
		LivroRequisicao requisicao = requisicaoValida().isbn("123").autorIds(Set.of()).build();

		mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.isbn").exists())
				.andExpect(jsonPath("$.campos.autorIds").exists());
		verifyNoInteractions(servico);
	}

	@Test
	void deveRetornar400ParaQuantidadeNaoPositiva() throws Exception {
		LivroRequisicao requisicao = requisicaoValida().quantidadeTotal(0).build();

		mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.campos.quantidadeTotal").exists());
	}

	@Test
	void deveRetornar404QuandoAutorNaoExiste() throws Exception {
		when(servico.criar(any(ComandoLivro.class), eq(2))).thenThrow(new RecursoNaoEncontradoException("Autor não encontrado."));

		mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(requisicaoValida().build())))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar409ParaIsbnDuplicado() throws Exception {
		when(servico.criar(any(ComandoLivro.class), eq(2))).thenThrow(new ConflitoException("Já existe um livro com este ISBN."));

		mvc.perform(post("/livros").contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsString(requisicaoValida().build())))
				.andExpect(status().isConflict());
	}

	@Test
	void deveRepassarFiltrosDeTituloEAutorNaListagem() throws Exception {
		when(servico.listar(new FiltroLivro("casm", 1L), new PaginaRequisicao(0, 5)))
				.thenReturn(new Pagina<>(List.of(LIVRO), 0, 5, 1, 1));

		mvc.perform(get("/livros").param("titulo", "casm").param("autorId", "1").param("page", "0").param("size", "5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.conteudo[0].titulo").value("Dom Casmurro"));
	}

	@Test
	void deveAtualizarLivroSemCampoDeQuantidade() throws Exception {
		when(servico.atualizar(eq(3L), any(ComandoLivro.class))).thenReturn(LIVRO);
		LivroAtualizacaoRequisicao requisicao = LivroAtualizacaoRequisicao.builder().titulo("Dom Casmurro")
				.isbn("9780306406157").categoria(Categoria.RARO).autorIds(Set.of(1L)).build();

		mvc.perform(put("/livros/3").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(requisicao)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(3));
	}

	@Test
	void deveExcluirLivroERetornar204() throws Exception {
		mvc.perform(delete("/livros/3")).andExpect(status().isNoContent());
	}
}
