package br.com.valueprojects.mock_spring.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;

/**
 * Prática 3 - @DataJpaTest: sobe só JPA/Hibernate + Spring Data + H2 em memória.
 * Cada teste roda numa transação que sofre rollback no final (banco limpo entre testes).
 */
@DataJpaTest
class JogoRepositoryTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private JogoRepository jogos;

	@Autowired
	private ParticipanteRepository participantes;

	private Calendar diasAtras(int dias) {
		Calendar data = Calendar.getInstance();
		data.add(Calendar.DAY_OF_MONTH, -dias);
		return data;
	}

	@Test
	void devePersistirJogoComResultadosEVencedorEmCascata() {
		Participante joao = new Participante("Joao");
		Participante maria = new Participante("Maria");
		Jogo jogo = new CriadorDeJogo().para("Cata moedas")
			.resultado(joao, 150.0)
			.resultado(maria, 200.0)
			.finaliza()
			.constroi();
		jogo.defineVencedor(maria);

		int id = jogos.save(jogo).getId();
		// força o SQL (flush) e esvazia o cache de 1º nível para ler de verdade do banco
		em.flush();
		em.clear();

		Jogo doBanco = jogos.findById(id).orElseThrow();
		assertEquals("Cata moedas", doBanco.getDescricao());
		assertTrue(doBanco.isFinalizado());
		assertEquals(2, doBanco.getResultados().size());
		assertEquals("Joao", doBanco.getResultados().get(0).getParticipante().getNome());
		assertEquals(200.0, doBanco.getResultados().get(1).getMetrica(), 0.00001);
		assertEquals("Maria", doBanco.getVencedor().getNome());
	}

	@Test
	void deveFiltrarJogosPorStatus() {
		em.persist(new CriadorDeJogo().para("Em andamento").constroi());
		em.persist(new CriadorDeJogo().para("Finalizado").finaliza().constroi());

		List<Jogo> finalizados = jogos.findByFinalizado(true);

		assertEquals(1, finalizados.size());
		assertEquals("Finalizado", finalizados.get(0).getDescricao());
	}

	@Test
	void deveBuscarApenasJogosEmAndamentoDaSemanaAnterior() {
		em.persist(new CriadorDeJogo().para("Semana passada").naData(diasAtras(8)).constroi());
		em.persist(new CriadorDeJogo().para("Ontem").naData(diasAtras(1)).constroi());
		em.persist(new CriadorDeJogo().para("Antigo finalizado").naData(diasAtras(30)).finaliza().constroi());

		List<Jogo> resultado = jogos.emAndamentoIniciadosAte(diasAtras(7));

		assertEquals(1, resultado.size());
		assertEquals("Semana passada", resultado.get(0).getDescricao());
	}

	@Test
	void deveEncontrarParticipantePeloNome() {
		em.persist(new Participante("Katia"));

		assertEquals("Katia", participantes.findFirstByNome("Katia").orElseThrow().getNome());
		assertTrue(participantes.findFirstByNome("Ninguem").isEmpty());
	}
}
