package com.apibiblioteca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.apibiblioteca.compartilhado.IntegracaoBase;

/** CA22: o contexto sobe com Flyway aplicando V1 e V2 e o Hibernate validando o schema. */
class AplicacaoIntegracaoTest extends IntegracaoBase {

	@Test
	void deveAplicarMigrationsEValidarSchemaAoSubir() {
		Integer versoes = jdbc.queryForObject(
				"select count(*) from flyway_schema_history where success and version in ('1', '2')", Integer.class);

		assertThat(versoes).isEqualTo(2);
	}
}
