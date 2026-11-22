package com.apibiblioteca.compartilhado.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/** Metadados exibidos no Swagger UI. */
@Configuration
public class OpenApiConfiguracao {

	/** Título, versão e descrição da documentação OpenAPI. */
	@Bean
	public OpenAPI documentacao() {
		return new OpenAPI().info(new Info().title("API de Biblioteca").version("v1")
				.description("Autores, livros, leitores e empréstimos com multa por atraso."));
	}
}
