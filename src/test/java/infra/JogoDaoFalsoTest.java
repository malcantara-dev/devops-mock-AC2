package infra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;

public class JogoDaoFalsoTest {

	@Test
	public void deveSepararJogosFinalizadosDosEmAndamento() {
		JogoDaoFalso dao = new JogoDaoFalso();
		Jogo emAndamento = new CriadorDeJogo().para("Cata moedas").constroi();
		Jogo finalizado = new CriadorDeJogo().para("Derruba barreiras").finaliza().constroi();

		dao.salva(emAndamento);
		dao.salva(finalizado);
		dao.atualiza(finalizado);

		assertEquals(List.of(emAndamento), dao.emAndamento());
		assertEquals(List.of(finalizado), dao.finalizados());
	}
}
