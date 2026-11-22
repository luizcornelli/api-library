package com.apibiblioteca.compartilhado.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.apibiblioteca.compartilhado.dominio.ConflitoException;
import com.apibiblioteca.compartilhado.dominio.RecursoNaoEncontradoException;

/**
 * Traduz exceções em respostas ProblemDetail (RFC 9457). Herda o tratamento padrão do Spring
 * para 400, 405 etc. e nunca expõe stack trace nem detalhe interno ao cliente.
 */
@RestControllerAdvice
public class TratadorGlobalDeErros extends ResponseEntityExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

	/** 404 para recurso inexistente. */
	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException pExcecao) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, pExcecao.getMessage());
	}

	/** 409 para regra de negócio violada pelo estado atual dos dados. */
	@ExceptionHandler(ConflitoException.class)
	public ProblemDetail tratarConflito(ConflitoException pExcecao) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, pExcecao.getMessage());
	}

	/**
	 * 409 para constraint violada que nenhum adaptador traduziu (ex.: exclusão concorrente).
	 * Não loga a mensagem do banco porque ela pode conter o valor duplicado (ex.: e-mail).
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail tratarIntegridade(DataIntegrityViolationException pExcecao) {
		LOG.warn("Violação de integridade de dados: {}", pExcecao.getMostSpecificCause().getClass().getSimpleName());
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "A operação conflita com dados existentes.");
	}

	/** 500 genérico: o detalhe vai só para o log. */
	@ExceptionHandler(Exception.class)
	public ProblemDetail tratarInesperado(Exception pExcecao) {
		LOG.error("Erro inesperado ao processar a requisição", pExcecao);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.");
	}

	/** 400 com a lista de campos inválidos, para o cliente corrigir o payload. */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException pExcecao,
			HttpHeaders pCabecalhos, HttpStatusCode pStatus, WebRequest pRequisicao) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dados inválidos.");
		problema.setProperty("campos", camposInvalidos(pExcecao));
		return ResponseEntity.badRequest().body(problema);
	}

	/** Monta campo → mensagem, mantendo a primeira mensagem de cada campo. */
	private Map<String, String> camposInvalidos(MethodArgumentNotValidException pExcecao) {
		return pExcecao.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField,
				erro -> Objects.requireNonNullElse(erro.getDefaultMessage(), "inválido"), (primeira, outra) -> primeira,
				LinkedHashMap::new));
	}
}
