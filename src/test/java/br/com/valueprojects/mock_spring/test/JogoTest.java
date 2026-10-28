package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.builder.*;
import br.com.valueprojects.mock_spring.model.*;

public class JogoTest {
	
		
	@Test
	public void deveTerJogoComUnicoParticipante() {
		Jogo jogo = new Jogo("Jogo de corrida");
		assertEquals(0, jogo.getResultados().size());

		jogo.anota(new Resultado(new Participante("Leonardo"), 150));

		assertEquals(1, jogo.getResultados().size());

		assertEquals(150.0, jogo.getResultados().get(0).getMetrica(), 0.00001);
	}
	
	

	@Test
	public void deveTerVariosResultados() {
		Jogo jogo = new CriadorDeJogo()
        .para("Cata moedas")
        .resultado(new Participante("Nelson"), 150.0)
        .resultado(new Participante("Pedro"), 200.0)
        .constroi();

		assertEquals(2, jogo.getResultados().size());
		assertEquals(150.0, jogo.getResultados().get(0).getMetrica(), 0.00001);
		assertEquals(200.0, jogo.getResultados().get(1).getMetrica(), 0.00001);
		
	}
	
	@Test
	public void naoDeveAceitarDoisResultadosDoMesmoParticipante(){

		Jogo jogo = new Jogo("Ca�a pe�as");
		Participante leonardo = new Participante("Leonardo");

		jogo.anota(new Resultado(leonardo, 500.0));
		//deve ser ignorado
		jogo.anota(new Resultado(leonardo, 600.0));

		assertEquals(1, jogo.getResultados().size());
        assertEquals(500, jogo.getResultados().get(0).getMetrica(), 0.00001);
	}

	@Test
	public void naoDeveAceitarMaisDoQueCincoResultadosDoMesmoParticipante() {
		Participante nelson = new Participante("Nelson");
		Participante pedro = new Participante("Pedro");

		CriadorDeJogo criador = new CriadorDeJogo().para("Cata moedas");
		for (int i = 0; i < 6; i++) {
			criador.resultado(nelson, 100.0 + i).resultado(pedro, 200.0 + i);
		}
		// o 6º resultado de cada um deve ser ignorado
		Jogo jogo = criador.resultado(nelson, 999.0).constroi();

		assertEquals(10, jogo.getResultados().size());
		assertEquals(204.0, jogo.getResultados().get(9).getMetrica(), 0.00001);
	}

	@Test
	public void deveFinalizarERegistrarOVencedor() {
		Participante leonardo = new Participante(7, "Leonardo");
		Jogo jogo = new Jogo("Derruba barreiras");

		jogo.finaliza();
		jogo.defineVencedor(leonardo);
		jogo.setId(42);

		assertTrue(jogo.isFinalizado());
		assertEquals(leonardo, jogo.getVencedor());
		assertEquals(42, jogo.getId());
		assertEquals("Derruba barreiras", jogo.getDescricao());
	}

	@Test
	public void naoDeveExporADataInternaParaAlteracao() {
		Calendar data = Calendar.getInstance();
		data.set(2024, Calendar.JANUARY, 10);
		Jogo jogo = new Jogo("Cata moedas", data);

		jogo.getData().add(Calendar.YEAR, 10);

		assertEquals(2024, jogo.getData().get(Calendar.YEAR));
	}

 }
