package com.apibiblioteca.compartilhado.configuracao;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Fornece o relógio da aplicação; injetado para os testes poderem fixar a data. */
@Configuration
public class RelogioConfiguracao {

	/** Relógio no fuso configurado (Recife), usado para definir "hoje" em empréstimos e devoluções. */
	@Bean
	public Clock relogio(@Value("${biblioteca.fuso-horario}") String pFusoHorario) {
		return Clock.system(ZoneId.of(pFusoHorario));
	}
}
