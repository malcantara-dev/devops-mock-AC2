package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;

public class CriadorDeJogoTest {

	@Test
	public void deveConstruirJogoFinalizadoComId() {
		CriadorDeJogo criador = new CriadorDeJogo().para("Cata moedas").comId(10).finaliza();

		Jogo jogo = criador.constroi();

		assertTrue(criador.isFinalizado());
		assertTrue(jogo.isFinalizado());
		assertEquals(10, criador.getId());
		assertEquals(10, jogo.getId());
	}

	@Test
	public void deveConstruirJogoEmAndamentoPorPadrao() {
		CriadorDeJogo criador = new CriadorDeJogo().para("Cata moedas");

		assertFalse(criador.isFinalizado());
		assertFalse(criador.constroi().isFinalizado());
	}
}
