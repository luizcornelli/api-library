package com.apibiblioteca.emprestimo.dominio.multa;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.apibiblioteca.livro.dominio.Categoria;

/** Escolhe a estratégia de multa pela categoria do livro. Nova regra = nova estratégia, sem mexer aqui. */
public class SeletorEstrategiaMulta {

	private final Map<Categoria, EstrategiaMulta> estrategias = new EnumMap<>(Categoria.class);

	/** Indexa as estratégias disponíveis pela categoria que cada uma atende. */
	public SeletorEstrategiaMulta(List<EstrategiaMulta> pEstrategias) {
		pEstrategias.forEach(estrategia -> estrategias.put(estrategia.categoria(), estrategia));
	}

	/** Estratégia da categoria; falha alto se faltar configuração, em vez de cobrar multa errada. */
	public EstrategiaMulta para(Categoria pCategoria) {
		EstrategiaMulta estrategia = estrategias.get(pCategoria);
		if (estrategia == null) {
			throw new IllegalStateException("Sem estratégia de multa para a categoria " + pCategoria);
		}
		return estrategia;
	}
}
