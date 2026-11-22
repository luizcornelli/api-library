package com.apibiblioteca.compartilhado;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Dispara tarefas em threads separadas liberadas ao mesmo tempo, para simular requisições simultâneas. */
public final class Concorrencia {

	private Concorrencia() {
	}

	/** Resultado de uma tarefa: o valor ou a exceção lançada. */
	public record Resultado<T>(T valor, Throwable erro) {
	}

	/** Executa as tarefas em paralelo, partindo juntas, e devolve o resultado de cada uma. */
	@SafeVarargs
	public static <T> List<Resultado<T>> executarJuntas(Callable<T>... pTarefas) throws InterruptedException {
		CountDownLatch largada = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(pTarefas.length);
		try {
			List<Future<T>> futuros = new ArrayList<>();
			for (Callable<T> tarefa : pTarefas) {
				futuros.add(executor.submit(() -> {
					largada.await();
					return tarefa.call();
				}));
			}
			largada.countDown();
			List<Resultado<T>> resultados = new ArrayList<>();
			for (Future<T> futuro : futuros) {
				resultados.add(aguardar(futuro));
			}
			return resultados;
		} finally {
			executor.shutdownNow();
		}
	}

	private static <T> Resultado<T> aguardar(Future<T> pFuturo) throws InterruptedException {
		try {
			return new Resultado<>(pFuturo.get(30, TimeUnit.SECONDS), null);
		} catch (java.util.concurrent.ExecutionException excecao) {
			return new Resultado<>(null, excecao.getCause());
		} catch (java.util.concurrent.TimeoutException excecao) {
			throw new IllegalStateException("Tarefa concorrente não terminou em 30 s", excecao);
		}
	}
}
