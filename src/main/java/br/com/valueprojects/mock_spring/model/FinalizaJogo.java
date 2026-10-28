package br.com.valueprojects.mock_spring.model;

import java.util.Calendar;
import java.util.List;

import infra.EnviadorDeSms;
import infra.JogoDao;



public class FinalizaJogo {

	private int total = 0;
	private final JogoDao dao;
	private final EnviadorDeSms sms;

	// injeção de dependência pelo construtor: o teste entrega mocks no lugar do DAO e do SMS reais
	public FinalizaJogo(JogoDao dao, EnviadorDeSms sms) {
		this.dao = dao;
		this.sms = sms;
	}

	public void finaliza() {
		List<Jogo> todosJogosEmAndamento = dao.emAndamento();

		for (Jogo jogo : todosJogosEmAndamento) {
			if (iniciouSemanaAnterior(jogo)) {
				jogo.finaliza();
				identificaVencedor(jogo);

				try {
					dao.atualiza(jogo);
				} catch (RuntimeException e) {
					// não salvou: em hipótese alguma o SMS pode sair; segue para o próximo jogo
					continue;
				}
				total++;

				// só chega aqui depois de salvo na base
				if (jogo.getVencedor() != null) {
					sms.envia(jogo.getVencedor(), mensagemDeVitoria(jogo));
				}
			}
		}
	}

	private void identificaVencedor(Jogo jogo) {
		if (jogo.getResultados().isEmpty()) return;

		Juiz juiz = new Juiz();
		juiz.julga(jogo);
		jogo.defineVencedor(juiz.getVencedor());
	}

	private String mensagemDeVitoria(Jogo jogo) {
		return "Parabéns, " + jogo.getVencedor().getNome() + "! Você venceu o jogo " + jogo.getDescricao() + ".";
	}

	private boolean iniciouSemanaAnterior(Jogo jogo) {
		return diasEntre(jogo.getData(), Calendar.getInstance()) >= 7;
	}

	private int diasEntre(Calendar inicio, Calendar fim) {
		Calendar data = (Calendar) inicio.clone();
		int diasNoIntervalo = 0;
		while (data.before(fim)) {
			data.add(Calendar.DAY_OF_MONTH, 1);
			diasNoIntervalo++;
		}

		return diasNoIntervalo;
	}

	public int getTotalFinalizados() {
		return total;
	}
}
