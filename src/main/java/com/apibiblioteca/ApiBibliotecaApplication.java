package com.apibiblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Ponto de entrada da API de biblioteca. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiBibliotecaApplication {

	/** Sobe a aplicação Spring Boot. */
	public static void main(String[] pArgumentos) {
		SpringApplication.run(ApiBibliotecaApplication.class, pArgumentos);
	}

}
