package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Participante;

public class ParticipanteTest {

	@Test
	public void deveSerIgualQuandoIdENomeSaoIguais() {
		Participante joao = new Participante(1, "Joao");

		assertEquals(joao, joao);
		assertEquals(joao, new Participante(1, "Joao"));
		assertEquals(joao.hashCode(), new Participante(1, "Joao").hashCode());
		assertEquals(new Participante(1, null), new Participante(1, null));
	}

	@Test
	public void deveSerDiferenteQuandoIdNomeOuTipoDiferem() {
		Participante joao = new Participante(1, "Joao");

		assertNotEquals(joao, null);
		assertNotEquals(joao, "Joao");
		assertNotEquals(joao, new Participante(2, "Joao"));
		assertNotEquals(joao, new Participante(1, "Maria"));
		assertNotEquals(new Participante(1, null), joao);
		assertNotEquals(joao, new Participante(1, null));
	}
}
