package br.com.valueprojects.mock_spring.service;

public class JogoNaoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JogoNaoEncontradoException(int id) {
		super("Jogo " + id + " não encontrado");
	}
}
