package com.apibiblioteca.livro.infraestrutura;

import java.util.Set;
import java.util.stream.Collectors;

import com.apibiblioteca.autor.infraestrutura.AutorEntidade;
import com.apibiblioteca.autor.infraestrutura.AutorMapeador;
import com.apibiblioteca.livro.dominio.DadosLivro;
import com.apibiblioteca.livro.dominio.Estoque;
import com.apibiblioteca.livro.dominio.Isbn;
import com.apibiblioteca.livro.dominio.Livro;

/** Converte livro entre domínio e entidade JPA. */
public final class LivroMapeador {

	private LivroMapeador() {
	}

	/** Entidade → domínio. Acessa os autores: a consulta de origem precisa trazê-los (JOIN FETCH). */
	public static Livro paraDominio(LivroEntidade pEntidade) {
		DadosLivro dados = new DadosLivro(pEntidade.getTitulo(), new Isbn(pEntidade.getIsbn()),
				pEntidade.getAnoPublicacao(), pEntidade.getCategoria(),
				pEntidade.getAutores().stream().map(AutorMapeador::paraDominio).collect(Collectors.toSet()));
		Estoque estoque = new Estoque(pEntidade.getQuantidadeTotal(), pEntidade.getQuantidadeDisponivel());
		return new Livro(pEntidade.getId(), dados, estoque);
	}

	/** Domínio → entidade. */
	public static LivroEntidade paraEntidade(Livro pLivro) {
		DadosLivro dados = pLivro.getDados();
		LivroEntidadeDados colunas = new LivroEntidadeDados(dados.titulo(), dados.isbn().valor(), dados.anoPublicacao(),
				dados.categoria(), pLivro.getEstoque().total(), pLivro.getEstoque().disponivel());
		return new LivroEntidade(pLivro.getId(), colunas, autores(dados));
	}

	/** Converte os autores do domínio em entidades (apenas referências por id). */
	private static Set<AutorEntidade> autores(DadosLivro pDados) {
		return pDados.autores().stream().map(AutorMapeador::paraEntidade).collect(Collectors.toSet());
	}
}
