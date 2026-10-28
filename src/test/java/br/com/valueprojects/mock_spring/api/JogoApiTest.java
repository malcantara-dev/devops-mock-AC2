package br.com.valueprojects.mock_spring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.controller.NovoJogoRequest;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.repository.JogoRepository;
import br.com.valueprojects.mock_spring.repository.ParticipanteRepository;
import br.com.valueprojects.mock_spring.service.ResultadoJulgamento;

/**
 * Prática 3 - teste de API com mocks: Tomcat real numa porta aleatória, requisições HTTP de verdade
 * (TestRestTemplate), controller + service reais; só a fronteira com o banco é mockada (@MockBean).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class JogoApiTest {

	@Autowired
	private TestRestTemplate http;

	// substitui o bean real no ApplicationContext pelo mock do Mockito
	@MockBean
	private JogoRepository jogos;

	@MockBean
	private ParticipanteRepository participantes;

	@Test
	void postDeveCriarJogoPelaApi() {
		// stub: o "banco" devolve o jogo com o id gerado
		when(jogos.save(any(Jogo.class))).thenAnswer(inv -> {
			Jogo jogo = inv.getArgument(0);
			jogo.setId(10);
			return jogo;
		});

		ResponseEntity<Jogo> resposta = http.postForEntity("/jogos", new NovoJogoRequest("Cata moedas"), Jogo.class);

		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertTrue(resposta.getHeaders().getLocation().toString().endsWith("/jogos/10"));
		// mock: confirma que a API realmente pediu para salvar
		verify(jogos).save(any(Jogo.class));
	}

	@Test
	void postJulgarDeveDevolverOVencedor() {
		Jogo jogo = new CriadorDeJogo().para("Derruba barreiras").comId(1)
			.resultado(new Participante(1, "Joao"), 90.0)
			.resultado(new Participante(2, "Maria"), 94.0)
			.constroi();
		when(jogos.findById(1)).thenReturn(Optional.of(jogo));

		ResponseEntity<ResultadoJulgamento> resposta =
			http.postForEntity("/jogos/1/julgar", null, ResultadoJulgamento.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertEquals(new ResultadoJulgamento(94.0, 90.0, "Maria"), resposta.getBody());
	}

	@Test
	@SuppressWarnings("rawtypes")
	void getDeJogoInexistenteDeveDevolver404ProblemDetail() {
		when(jogos.findById(99)).thenReturn(Optional.empty());

		ResponseEntity<Map> resposta = http.getForEntity("/jogos/99", Map.class);

		assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
		assertEquals("Jogo 99 não encontrado", resposta.getBody().get("detail"));
		verify(jogos, never()).save(any(Jogo.class));
	}
}
