package br.com.valueprojects.mock_spring.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Juiz;
import br.com.valueprojects.mock_spring.model.Participante;
import br.com.valueprojects.mock_spring.model.Resultado;
import br.com.valueprojects.mock_spring.repository.JogoRepository;
import br.com.valueprojects.mock_spring.repository.ParticipanteRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Service
@Transactional
public class JogoService {

	private final JogoRepository jogos;
	private final ParticipanteRepository participantes;
	private final Counter jogosCriados;
	private final Counter resultadosAnotados;
	private final Counter jogosJulgados;

	// métricas de negócio expostas em /actuator/prometheus (Prática 4)
	public JogoService(JogoRepository jogos, ParticipanteRepository participantes, MeterRegistry registry) {
		this.jogos = jogos;
		this.participantes = participantes;
		this.jogosCriados = Counter.builder("jogos.criados")
				.description("Jogos criados pela API")
				.register(registry);
		this.resultadosAnotados = Counter.builder("jogos.resultados.anotados")
				.description("Resultados anotados nos jogos")
				.register(registry);
		this.jogosJulgados = Counter.builder("jogos.julgados")
				.description("Julgamentos realizados")
				.register(registry);
	}

	public Jogo cria(String descricao) {
		Jogo jogo = jogos.save(new Jogo(descricao));
		jogosCriados.increment();
		return jogo;
	}

	@Transactional(readOnly = true)
	public List<Jogo> lista() {
		return jogos.findAll();
	}

	@Transactional(readOnly = true)
	public Jogo busca(int id) {
		return jogos.findById(id).orElseThrow(() -> new JogoNaoEncontradoException(id));
	}

	public Jogo anotaResultado(int id, String nomeParticipante, double metrica) {
		Jogo jogo = busca(id);
		Participante participante = participantes.findFirstByNome(nomeParticipante)
				.orElseGet(() -> new Participante(nomeParticipante));

		// jogo está gerenciado pela transação: o novo resultado é persistido no commit (cascade)
		jogo.anota(new Resultado(participante, metrica));
		resultadosAnotados.increment();
		return jogo;
	}

	@Transactional(readOnly = true)
	public ResultadoJulgamento julga(int id) {
		Jogo jogo = busca(id);

		// um Juiz novo por julgamento: ele acumula estado entre chamadas
		Juiz juiz = new Juiz();
		juiz.julga(jogo);
		jogosJulgados.increment();

		return new ResultadoJulgamento(juiz.getPrimeiroColocado(), juiz.getUltimoColocado(),
				juiz.getVencedor().getNome());
	}
}
