package com.apibiblioteca.compartilhado.configuracao;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Proteção de dados no acesso ao banco. O driver do PostgreSQL inclui na mensagem de erro o
 * valor que violou a constraint (ex.: "Key (email)=(...) already exists"), e essa mensagem vai
 * para os logs. Fica em código, e não no application.yaml, para a proteção não depender de
 * configuração por ambiente.
 */
@Configuration
public class BancoDeDadosConfiguracao {

	/** Desliga o detalhe do erro do servidor (logServerErrorDetail) em todo pool Hikari. */
	@Bean
	static BeanPostProcessor ocultarDetalheDeErroDoBanco() {
		return new BeanPostProcessor() {

			/** Ajusta o pool antes de ele abrir a primeira conexão. */
			@Override
			public Object postProcessBeforeInitialization(Object pBean, String pNome) {
				if (pBean instanceof HikariDataSource dataSource) {
					dataSource.addDataSourceProperty("logServerErrorDetail", "false");
				}
				return pBean;
			}
		};
	}
}
