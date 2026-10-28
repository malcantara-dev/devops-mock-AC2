package br.com.valueprojects.mock_spring.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * Prática 3/4 - teste de integração SEM mocks: controller -> service -> JPA -> H2,
 * e o endpoint /actuator/prometheus que o Prometheus vai raspar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability // em testes o Spring desliga o export de métricas por padrão
class JogoApiIntegrationTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void deveCriarAnotarEJulgarUmJogoDePontaAPonta() throws Exception {
		String criado = mvc.perform(post("/jogos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"descricao\":\"Cata moedas\"}"))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		int id = JsonPath.read(criado, "$.id");

		mvc.perform(post("/jogos/{id}/resultados", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"participante\":\"Joao\",\"metrica\":90.0}"))
			.andExpect(status().isOk());
		mvc.perform(post("/jogos/{id}/resultados", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"participante\":\"Maria\",\"metrica\":94.0}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultados.length()").value(2));

		mvc.perform(get("/jogos/{id}", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.resultados[1].participante.nome").value("Maria"));

		mvc.perform(post("/jogos/{id}/julgar", id))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.vencedor").value("Maria"));

		mvc.perform(get("/jogos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id == " + id + ")].descricao").value("Cata moedas"));
	}

	@Test
	void naoDeveJulgarJogoSemResultados() throws Exception {
		String criado = mvc.perform(post("/jogos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"descricao\":\"Vazio\"}"))
			.andReturn().getResponse().getContentAsString();
		int id = JsonPath.read(criado, "$.id");

		mvc.perform(post("/jogos/{id}/julgar", id))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void deveExporMetricasNoFormatoDoPrometheus() throws Exception {
		mvc.perform(post("/jogos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"descricao\":\"Métrica\"}"))
			.andExpect(status().isCreated());

		mvc.perform(get("/actuator/prometheus"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("jogos_criados_total")))
			.andExpect(content().string(containsString("http_server_requests_seconds")))
			.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}

	@Test
	void deveExporHealthParaOrquestrador() throws Exception {
		mvc.perform(get("/actuator/health/liveness"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}
}
