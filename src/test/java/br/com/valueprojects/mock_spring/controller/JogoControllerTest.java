package br.com.valueprojects.mock_spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;

public class JogoControllerTest {

	@Test
	public void deveCriarListarEJulgarJogos() {
		JogoController controller = new JogoController();

		Jogo jogo = controller.criarJogo("Cata moedas");
		jogo.anota(new Resultado(new Participante("Ana"), 80.0));
		jogo.anota(new Resultado(new Participante("Bia"), 95.0));

		assertEquals(List.of(jogo), controller.listarJogos());
		assertTrue(controller.julgarJogo(0).startsWith("Primeiro colocado: 95.0"));
	}
}
