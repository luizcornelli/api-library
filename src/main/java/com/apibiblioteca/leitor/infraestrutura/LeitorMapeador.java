package com.apibiblioteca.leitor.infraestrutura;

import com.apibiblioteca.leitor.dominio.Email;
import com.apibiblioteca.leitor.dominio.Leitor;

/** Converte leitor entre domínio e entidade JPA. */
public final class LeitorMapeador {

	private LeitorMapeador() {
	}

	/** Entidade → domínio. */
	public static Leitor paraDominio(LeitorEntidade pEntidade) {
		return new Leitor(pEntidade.getId(), pEntidade.getNome(), new Email(pEntidade.getEmail()));
	}

	/** Domínio → entidade. */
	public static LeitorEntidade paraEntidade(Leitor pLeitor) {
		return new LeitorEntidade(pLeitor.getId(), pLeitor.getNome(), pLeitor.getEmail().valor());
	}
}
