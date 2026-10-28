package br.com.valueprojects.mock_spring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.repository.JogoRepository;
import br.com.valueprojects.mock_spring.repository.ParticipanteRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class JogoServiceTest {

	@Mock
	private JogoRepository jogos;

	@Mock
	private ParticipanteRepository participantes;

	// registry real em memória: dá para conferir o valor das métricas sem Prometheus
	private SimpleMeterRegistry registry;
	private JogoService service;

	@BeforeEach
	void setUp() {
		registry = new SimpleMeterRegistry();
		service = new JogoService(jogos, participantes, registry);
	}

	@Test
	void deveCriarJogoEContarAMetrica() {
		when(jogos.save(any(Jogo.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

		Jogo jogo = service.cria("Cata moedas");

		assertEquals("Cata moedas", jogo.getDescricao());
		assertEquals(1.0, registry.get("jogos.criados").counter().count());
	}

	@Test
	void deveListarJogosDoRepositorio() {
		List<Jogo> todos = List.of(new Jogo("Cata moedas"));
		when(jogos.findAll()).thenReturn(todos);

		assertSame(todos, service.lista());
	}

	@Test
	void deveLancarExcecaoQuandoJogoNaoExiste() {
		when(jogos.findById(99)).thenReturn(Optional.empty());

		assertThrows(JogoNaoEncontradoException.class, () -> service.busca(99));
	}

	@Test
	void deveReaproveitarParticipanteExistenteAoAnotarResultado() {
		Participante joao = new Participante(1, "Joao");
		Jogo jogo = new CriadorDeJogo().para("Cata moedas").comId(1).constroi();
		when(jogos.findById(1)).thenReturn(Optional.of(jogo));
		when(participantes.findFirstByNome("Joao")).thenReturn(Optional.of(joao));

		service.anotaResultado(1, "Joao", 150.0);

		assertSame(joao, jogo.getResultados().get(0).getParticipante());
		assertEquals(1.0, registry.get("jogos.resultados.anotados").counter().count());
	}

	@Test
	void deveCriarParticipanteNovoAoAnotarResultado() {
		Jogo jogo = new CriadorDeJogo().para("Cata moedas").comId(1).constroi();
		when(jogos.findById(1)).thenReturn(Optional.of(jogo));
		when(participantes.findFirstByNome("Maria")).thenReturn(Optional.empty());

		service.anotaResultado(1, "Maria", 200.0);

		assertEquals("Maria", jogo.getResultados().get(0).getParticipante().getNome());
		verify(participantes).findFirstByNome("Maria");
	}

	@Test
	void deveJulgarComUmJuizNovoACadaChamada() {
		Jogo jogo1 = new CriadorDeJogo().para("Cata moedas")
			.resultado(new Participante(1, "Joao"), 300.0).constroi();
		Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras")
			.resultado(new Participante(2, "Maria"), 10.0).constroi();
		when(jogos.findById(1)).thenReturn(Optional.of(jogo1));
		when(jogos.findById(2)).thenReturn(Optional.of(jogo2));

		service.julga(1);
		ResultadoJulgamento segundo = service.julga(2);

		// com o Juiz compartilhado do controller antigo, o primeiro colocado seria 300
		assertEquals(new ResultadoJulgamento(10.0, 10.0, "Maria"), segundo);
		assertEquals(2.0, registry.get("jogos.julgados").counter().count());
	}
}
