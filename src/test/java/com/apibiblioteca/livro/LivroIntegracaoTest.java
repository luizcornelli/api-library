package com.apibiblioteca.livro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.apibiblioteca.autor.aplicacao.AutorServico;
import com.apibiblioteca.autor.dominio.Autor;
import com.apibiblioteca.compartilhado.Concorrencia;
import com.apibiblioteca.compartilhado.IntegracaoBase;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.livro.aplicacao.ComandoLivro;
import com.apibiblioteca.livro.aplicacao.LivroServico;
import com.apibiblioteca.livro.dominio.Categoria;
import com.apibiblioteca.livro.dominio.FiltroLivro;
import com.apibiblioteca.livro.dominio.Livro;

import jakarta.persistence.EntityManagerFactory;

class LivroIntegracaoTest extends IntegracaoBase {

	@Autowired
	private LivroServico livros;

	@Autowired
	private AutorServico autores;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	private Livro criarLivro(String pTitulo, String pIsbn, Set<Long> pAutorIds) {
		return livros.criar(new ComandoLivro(pTitulo, pIsbn, 2020, Categoria.COMUM, pAutorIds), 1);
	}

	@Test
	void deveGravarVinculoNParaNComAutores() {
		Autor primeiro = autores.criar("Autor Um", null);
		Autor segundo = autores.criar("Autor Dois", null);

		Livro criado = criarLivro("Obra Conjunta", isbnValido(1), Set.of(primeiro.getId(), segundo.getId()));

		assertThat(livros.buscar(criado.getId()).getDados().autores()).extracting(Autor::getNome)
				.containsExactlyInAnyOrder("Autor Um", "Autor Dois");
	}

	@Test
	void deveRejeitarIsbnDuplicadoMesmoComHifens() {
		Autor autor = autores.criar("Autor", null);
		criarLivro("Original", "9780306406157", Set.of(autor.getId()));

		assertThatThrownBy(() -> criarLivro("Copia", "978-0-306-40615-7", Set.of(autor.getId())))
				.isInstanceOf(ConflitoException.class).hasMessage("Já existe um livro com este ISBN.");
	}

	@Test
	void deveCriarApenasUmLivroQuandoMesmoIsbnChegaAoMesmoTempo() throws Exception {
		Long autorId = autores.criar("Autor", null).getId();

		List<Concorrencia.Resultado<Livro>> resultados = Concorrencia.executarJuntas(
				() -> criarLivro("A", "9780306406157", Set.of(autorId)),
				() -> criarLivro("B", "9780306406157", Set.of(autorId)));

		assertThat(resultados).filteredOn(r -> r.erro() == null).hasSize(1);
		assertThat(resultados).filteredOn(r -> r.erro() instanceof ConflitoException).hasSize(1);
		assertThat(jdbc.queryForObject("select count(*) from livro", Integer.class)).isEqualTo(1);
	}

	@Test
	void deveListarLivrosComAutoresSemNMaisUm() {
		Long autorA = autores.criar("A", null).getId();
		Long autorB = autores.criar("B", null).getId();
		for (int i = 1; i <= 12; i++) {
			criarLivro("Livro " + i, isbnValido(i), Set.of(autorA, autorB));
		}
		Statistics estatisticas = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();

		estatisticas.clear();
		Pagina<Livro> paginaPequena = livros.listar(new FiltroLivro(null, null), new PaginaRequisicao(0, 2));
		long consultasPaginaPequena = estatisticas.getPrepareStatementCount();
		estatisticas.clear();
		Pagina<Livro> paginaGrande = livros.listar(new FiltroLivro(null, null), new PaginaRequisicao(0, 10));
		long consultasPaginaGrande = estatisticas.getPrepareStatementCount();

		assertThat(paginaGrande.conteudo()).hasSize(10).allSatisfy(livro -> assertThat(livro.getDados().autores()).hasSize(2));
		assertThat(paginaPequena.totalElementos()).isEqualTo(12);
		assertThat(consultasPaginaGrande).isEqualTo(consultasPaginaPequena).isLessThanOrEqualTo(3);
	}

	@Test
	void deveFiltrarPorTrechoDoTituloEPorAutor() {
		Long autorA = autores.criar("A", null).getId();
		Long autorB = autores.criar("B", null).getId();
		criarLivro("Dom Casmurro", isbnValido(1), Set.of(autorA));
		criarLivro("Memórias Póstumas", isbnValido(2), Set.of(autorA));
		criarLivro("Grande Sertão", isbnValido(3), Set.of(autorB));

		Pagina<Livro> porTitulo = livros.listar(new FiltroLivro("CASM", null), new PaginaRequisicao(0, 10));
		Pagina<Livro> porAutor = livros.listar(new FiltroLivro(null, autorA), new PaginaRequisicao(0, 10));
		Pagina<Livro> combinados = livros.listar(new FiltroLivro("sert", autorA), new PaginaRequisicao(0, 10));

		assertThat(porTitulo.conteudo()).extracting(l -> l.getDados().titulo()).containsExactly("Dom Casmurro");
		assertThat(porAutor.conteudo()).extracting(l -> l.getDados().titulo())
				.containsExactly("Dom Casmurro", "Memórias Póstumas");
		assertThat(combinados.conteudo()).isEmpty();
	}

	@Test
	void deveAtualizarAutoresSemSobrescreverEstoque() {
		Long autorA = autores.criar("A", null).getId();
		Long autorB = autores.criar("B", null).getId();
		Livro livro = criarLivro("Original", isbnValido(1), Set.of(autorA));
		jdbc.update("update livro set quantidade_disponivel = 0 where id = ?", livro.getId());

		livros.atualizar(livro.getId(), new ComandoLivro("Novo Título", isbnValido(1), 2021, Categoria.RARO, Set.of(autorB)));

		Livro atualizado = livros.buscar(livro.getId());
		assertThat(atualizado.getDados().titulo()).isEqualTo("Novo Título");
		assertThat(atualizado.getDados().autores()).extracting(Autor::getId).containsExactly(autorB);
		assertThat(atualizado.getEstoque().disponivel()).isZero();
	}

	@Test
	void deveExcluirLivroSemEmprestimosEPermitirExcluirAutorDepois() {
		Long autorId = autores.criar("A", null).getId();
		Livro livro = criarLivro("Livro", isbnValido(1), Set.of(autorId));

		assertThatThrownBy(() -> autores.excluir(autorId)).isInstanceOf(ConflitoException.class);
		livros.excluir(livro.getId());
		autores.excluir(autorId);

		assertThat(jdbc.queryForObject("select count(*) from autor", Integer.class)).isZero();
	}
}
