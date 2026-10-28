package br.com.valueprojects.mock_spring.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Calendar;

import org.junit.jupiter.api.Test;

import br.com.valueprojects.mock_spring.model.Pagamento;

public class PagamentoTest {

	@Test
	public void deveGuardarValorEData() {
		Calendar hoje = Calendar.getInstance();

		Pagamento pagamento = new Pagamento(250.0, hoje);

		assertEquals(250.0, pagamento.getValor(), 0.00001);
		assertSame(hoje, pagamento.getData());
	}
}
