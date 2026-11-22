package com.apibiblioteca.leitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.apibiblioteca.compartilhado.Concorrencia;
import com.apibiblioteca.compartilhado.IntegracaoBase;
import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.leitor.aplicacao.LeitorServico;
import com.apibiblioteca.leitor.dominio.Leitor;

@ExtendWith(OutputCaptureExtension.class)
class LeitorIntegracaoTest extends IntegracaoBase {

	private static final String EMAIL = "leitor.ficticio@exemplo.test";

	@Autowired
	private LeitorServico leitores;

	@Test
	void deveRejeitarEmailDuplicadoSemDiferencaDeCaixaESemVazarEmail(CapturedOutput pSaida) {
		leitores.criar("Primeiro", EMAIL);

		assertThatThrownBy(() -> leitores.criar("Segundo", "Leitor.Ficticio@Exemplo.TEST"))
				.isInstanceOf(ConflitoException.class)
				.hasMessageNotContaining(EMAIL);
		assertThat(pSaida.getAll()).doesNotContainIgnoringCase(EMAIL);
	}

	@Test
	void deveCriarApenasUmLeitorQuandoMesmoEmailChegaAoMesmoTempo() throws Exception {
		List<Concorrencia.Resultado<Leitor>> resultados = Concorrencia.executarJuntas(
				() -> leitores.criar("A", EMAIL), () -> leitores.criar("B", EMAIL));

		assertThat(resultados).filteredOn(r -> r.erro() == null).hasSize(1);
		assertThat(resultados).filteredOn(r -> r.erro() instanceof ConflitoException).hasSize(1);
		assertThat(jdbc.queryForObject("select count(*) from leitor", Integer.class)).isEqualTo(1);
	}
}
