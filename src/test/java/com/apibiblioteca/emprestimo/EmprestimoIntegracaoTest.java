package com.apibiblioteca.emprestimo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.apibiblioteca.autor.aplicacao.AutorServico;
import com.apibiblioteca.compartilhado.Concorrencia;
import com.apibiblioteca.compartilhado.IntegracaoBase;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.Pagina;
import com.apibiblioteca.compartilhado.dominio.PaginaRequisicao;
import com.apibiblioteca.emprestimo.aplicacao.ConsultarEmprestimos;
import com.apibiblioteca.emprestimo.aplicacao.DevolverEmprestimo;
import com.apibiblioteca.emprestimo.aplicacao.RealizarEmprestimo;
import com.apibiblioteca.emprestimo.aplicacao.ResultadoEmprestimo;
import com.apibiblioteca.emprestimo.dominio.Emprestimo;
import com.apibiblioteca.emprestimo.dominio.StatusEmprestimo;
import com.apibiblioteca.leitor.aplicacao.LeitorServico;
import com.apibiblioteca.livro.aplicacao.ComandoLivro;
import com.apibiblioteca.livro.aplicacao.LivroServico;
import com.apibiblioteca.livro.dominio.Categoria;

class EmprestimoIntegracaoTest extends IntegracaoBase {

	@Autowired
	private RealizarEmprestimo realizar;

	@Autowired
	private DevolverEmprestimo devolver;

	@Autowired
	private ConsultarEmprestimos consultar;

	@Autowired
	private LivroServico livros;

	@Autowired
	private AutorServico autores;

	@Autowired
	private LeitorServico leitores;

	private Long livroComExemplares(int pQuantidade) {
		Long autorId = autores.criar("Autor", null).getId();
		return livros.criar(new ComandoLivro("Livro", isbnValido(1), 2020, Categoria.COMUM, Set.of(autorId)), pQuantidade).getId();
	}

	private Long leitor(String pApelido) {
		return leitores.criar("Leitor " + pApelido, pApelido + "@exemplo.test").getId();
	}

	private int disponivel(Long pLivroId) {
		return jdbc.queryForObject("select quantidade_disponivel from livro where id = ?", Integer.class, pLivroId);
	}

	@Test
	void deveBaixarExemplarAoEmprestarEDevolverAoReceber() {
		Long livroId = livroComExemplares(2);
		ResultadoEmprestimo resultado = realizar.executar(livroId, leitor("a"));

		assertThat(resultado.criado()).isTrue();
		assertThat(disponivel(livroId)).isEqualTo(1);

		Emprestimo devolvido = devolver.executar(resultado.emprestimo().getId());

		assertThat(devolvido.estaDevolvido()).isTrue();
		assertThat(disponivel(livroId)).isEqualTo(2);
	}

	@Test
	void deveRecusarEmprestimoSemExemplarDisponivel() {
		Long livroId = livroComExemplares(1);
		realizar.executar(livroId, leitor("a"));

		assertThatThrownBy(() -> realizar.executar(livroId, leitor("b"))).isInstanceOf(ConflitoException.class);
		assertThat(disponivel(livroId)).isZero();
	}

	@Test
	void deveDevolverMesmoEmprestimoSemConsumirExemplarQuandoPedidoSeRepete() {
		Long livroId = livroComExemplares(3);
		Long leitorId = leitor("a");

		ResultadoEmprestimo primeiro = realizar.executar(livroId, leitorId);
		ResultadoEmprestimo repetido = realizar.executar(livroId, leitorId);

		assertThat(repetido.criado()).isFalse();
		assertThat(repetido.emprestimo().getId()).isEqualTo(primeiro.emprestimo().getId());
		assertThat(disponivel(livroId)).isEqualTo(2);
	}

	@Test
	void deveEmprestarUltimoExemplarParaApenasUmQuandoDoisPedidosChegamJuntos() throws Exception {
		Long livroId = livroComExemplares(1);
		Long leitorA = leitor("a");
		Long leitorB = leitor("b");

		List<Concorrencia.Resultado<ResultadoEmprestimo>> resultados = Concorrencia.executarJuntas(
				() -> realizar.executar(livroId, leitorA), () -> realizar.executar(livroId, leitorB));

		assertThat(resultados).filteredOn(r -> r.erro() == null).hasSize(1);
		assertThat(resultados).filteredOn(r -> r.erro() instanceof ConflitoException).hasSize(1);
		assertThat(disponivel(livroId)).isZero();
		assertThat(jdbc.queryForObject("select count(*) from emprestimo", Integer.class)).isEqualTo(1);
	}

	@Test
	void deveCriarUmUnicoEmprestimoQuandoMesmoPedidoChegaDuasVezesAoMesmoTempo() throws Exception {
		Long livroId = livroComExemplares(3);
		Long leitorId = leitor("a");

		List<Concorrencia.Resultado<ResultadoEmprestimo>> resultados = Concorrencia.executarJuntas(
				() -> realizar.executar(livroId, leitorId), () -> realizar.executar(livroId, leitorId));

		assertThat(resultados).allSatisfy(r -> assertThat(r.erro()).isNull());
		assertThat(resultados).extracting(r -> r.valor().emprestimo().getId()).containsOnly(resultados.getFirst().valor().emprestimo().getId());
		assertThat(resultados).filteredOn(r -> r.valor().criado()).hasSize(1);
		assertThat(disponivel(livroId)).isEqualTo(2);
	}

	@Test
	void deveDevolverExemplarUmaUnicaVezQuandoDuasDevolucoesChegamJuntas() throws Exception {
		Long livroId = livroComExemplares(1);
		Long emprestimoId = realizar.executar(livroId, leitor("a")).emprestimo().getId();

		List<Concorrencia.Resultado<Emprestimo>> resultados = Concorrencia.executarJuntas(
				() -> devolver.executar(emprestimoId), () -> devolver.executar(emprestimoId));

		assertThat(resultados).allSatisfy(r -> assertThat(r.erro()).isNull());
		assertThat(resultados).allSatisfy(r -> assertThat(r.valor().estaDevolvido()).isTrue());
		assertThat(disponivel(livroId)).isEqualTo(1);
	}

	@Test
	void deveRetornarResultadoOriginalAoDevolverDeNovo() {
		Long livroId = livroComExemplares(1);
		Long emprestimoId = realizar.executar(livroId, leitor("a")).emprestimo().getId();
		Emprestimo primeira = devolver.executar(emprestimoId);

		Emprestimo segunda = devolver.executar(emprestimoId);

		assertThat(segunda.getSituacao()).isEqualTo(primeira.getSituacao());
		assertThat(disponivel(livroId)).isEqualTo(1);
	}

	@Test
	void deveFiltrarListagemPorStatus() {
		Long livroId = livroComExemplares(2);
		Long devolvidoId = realizar.executar(livroId, leitor("a")).emprestimo().getId();
		realizar.executar(livroId, leitor("b"));
		devolver.executar(devolvidoId);

		Pagina<Emprestimo> ativos = consultar.listar(StatusEmprestimo.ATIVO, new PaginaRequisicao(0, 10));
		Pagina<Emprestimo> devolvidos = consultar.listar(StatusEmprestimo.DEVOLVIDO, new PaginaRequisicao(0, 10));
		Pagina<Emprestimo> todos = consultar.listar(null, new PaginaRequisicao(0, 10));

		assertThat(ativos.conteudo()).extracting(Emprestimo::status).containsOnly(StatusEmprestimo.ATIVO).hasSize(1);
		assertThat(devolvidos.conteudo()).extracting(Emprestimo::getId).containsExactly(devolvidoId);
		assertThat(todos.totalElementos()).isEqualTo(2);
	}

	@Test
	void deveBloquearExclusaoDeLivroComEmprestimoMesmoDevolvido() {
		Long livroId = livroComExemplares(1);
		Long emprestimoId = realizar.executar(livroId, leitor("a")).emprestimo().getId();
		devolver.executar(emprestimoId);

		assertThatThrownBy(() -> livros.excluir(livroId)).isInstanceOf(ConflitoException.class);
	}
}
