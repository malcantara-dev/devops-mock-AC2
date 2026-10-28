package infra;

import java.util.ArrayList;
import java.util.List;

import br.com.valueprojects.mock_spring.model.Jogo;



public class JogoDaoFalso {

	// era static: o estado vazava de um teste para o outro e a ordem de execução mudava o resultado
	private final List<Jogo> jogos = new ArrayList<Jogo>();

	public void salva(Jogo jogo) {
		jogos.add(jogo);
	}

	public List<Jogo> finalizados() {

		List<Jogo> selecionados = new ArrayList<Jogo>();
		for(Jogo jogo : jogos) {
			if(jogo.isFinalizado()) selecionados.add(jogo);
		}

		return selecionados;
	}

	public List<Jogo> emAndamento() {

		List<Jogo> selecionados = new ArrayList<Jogo>();
		for(Jogo jogo : jogos) {
			if(!jogo.isFinalizado()) selecionados.add(jogo);
		}

		return selecionados;
	}

	public void atualiza(Jogo jogo) { /* sem ação! */ }
}
