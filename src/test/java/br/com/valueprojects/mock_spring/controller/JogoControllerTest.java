package br.com.valueprojects.mock_spring.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.service.JogoNaoEncontradoException;
import br.com.valueprojects.mock_spring.service.JogoService;
import br.com.valueprojects.mock_spring.service.ResultadoJulgamento;

/**
 * Prática 3 - MockMvc: sobe só a camada web (DispatcherServlet, conversores JSON,
 * ControllerAdvice) sem servidor HTTP e sem banco; o service é um mock.
 */
@WebMvcTest(JogoController.class)
class JogoControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private JogoService service;

	@Test
	void deveCriarJogoERetornar201ComLocation() throws Exception {
		when(service.cria("Cata moedas")).thenReturn(new CriadorDeJogo().para("Cata moedas").comId(1).constroi());

		mvc.perform(post("/jogos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"descricao\":\"Cata moedas\"}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", endsWith("/jogos/1")))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.descricao").value("Cata moedas"))
			.andExpect(jsonPath("$.finalizado").value(false));
	}

	@Test
	void deveListarJogos() throws Exception {
		when(service.lista()).thenReturn(List.of(new Jogo("Cata moedas"), new Jogo("Derruba barreiras")));

		mvc.perform(get("/jogos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[1].descricao").value("Derruba barreiras"));
	}

	@Test
	void deveBuscarJogoPorId() throws Exception {
		when(service.busca(1)).thenReturn(new CriadorDeJogo().para("Cata moedas").comId(1).constroi());

		mvc.perform(get("/jogos/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.descricao").value("Cata moedas"));
	}

	@Test
	void deveAnotarResultado() throws Exception {
		Jogo jogo = new CriadorDeJogo().para("Cata moedas").comId(1)
			.resultado(new Participante(1, "Joao"), 150.0).constroi();
		when(service.anotaResultado(1, "Joao", 150.0)).thenReturn(jogo);

		mvc.perform(post("/jogos/1/resultados")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"participante\":\"Joao\",\"metrica\":150.0}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultados[0].participante.nome").value("Joao"))
			.andExpect(jsonPath("$.resultados[0].metrica").value(150.0));

		verify(service).anotaResultado(1, "Joao", 150.0);
	}

	@Test
	void deveJulgarJogo() throws Exception {
		when(service.julga(1)).thenReturn(new ResultadoJulgamento(94.0, 90.0, "Maria"));

		mvc.perform(post("/jogos/1/julgar"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.primeiroColocado").value(94.0))
			.andExpect(jsonPath("$.ultimoColocado").value(90.0))
			.andExpect(jsonPath("$.vencedor").value("Maria"));
	}

	@Test
	void deveRetornar404QuandoJogoNaoExiste() throws Exception {
		when(service.busca(99)).thenThrow(new JogoNaoEncontradoException(99));

		mvc.perform(get("/jogos/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Jogo 99 não encontrado"));
	}

	@Test
	void deveRetornar422AoJulgarJogoSemResultados() throws Exception {
		when(service.julga(1)).thenThrow(new IllegalStateException("Sem resultados não há julgamento!"));

		mvc.perform(post("/jogos/1/julgar"))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.status").value(422));
	}
}
