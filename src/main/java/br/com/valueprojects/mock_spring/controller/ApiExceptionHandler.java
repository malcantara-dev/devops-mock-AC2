package br.com.valueprojects.mock_spring.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.valueprojects.mock_spring.service.JogoNaoEncontradoException;

// respostas de erro no formato RFC 7807 (application/problem+json)
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(JogoNaoEncontradoException.class)
	public ProblemDetail jogoNaoEncontrado(JogoNaoEncontradoException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
	}

	// ex.: julgar um jogo sem resultados
	@ExceptionHandler(IllegalStateException.class)
	public ProblemDetail regraDeNegocioViolada(IllegalStateException e) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
	}
}
