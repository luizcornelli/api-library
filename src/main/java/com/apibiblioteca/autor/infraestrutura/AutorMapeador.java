package com.apibiblioteca.autor.infraestrutura;

import com.apibiblioteca.autor.dominio.Autor;

/** Converte autor entre domínio e entidade JPA. */
public final class AutorMapeador {

	private AutorMapeador() {
	}

	/** Entidade → domínio. */
	public static Autor paraDominio(AutorEntidade pEntidade) {
		return new Autor(pEntidade.getId(), pEntidade.getNome(), pEntidade.getNacionalidade());
	}

	/** Domínio → entidade. */
	public static AutorEntidade paraEntidade(Autor pAutor) {
		return new AutorEntidade(pAutor.getId(), pAutor.getNome(), pAutor.getNacionalidade());
	}
}
