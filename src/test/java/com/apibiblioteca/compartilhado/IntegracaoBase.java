package com.apibiblioteca.compartilhado;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.apibiblioteca.TestcontainersConfiguration;

/** Base dos testes de integração: PostgreSQL real, migrations do Flyway e tabelas limpas a cada teste. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class IntegracaoBase {

	@Autowired
	protected JdbcTemplate jdbc;

	@BeforeEach
	void limparTabelas() {
		jdbc.execute("truncate table emprestimo, livro_autor, livro, autor, leitor restart identity cascade");
	}

	/** Gera um ISBN-13 válido (dígito verificador correto) a partir de um número sequencial. */
	protected static String isbnValido(int pSequencia) {
		String prefixo = String.format("978%09d", pSequencia);
		int soma = 0;
		for (int i = 0; i < prefixo.length(); i++) {
			soma += Character.getNumericValue(prefixo.charAt(i)) * (i % 2 == 0 ? 1 : 3);
		}
		return prefixo + (10 - soma % 10) % 10;
	}
}
