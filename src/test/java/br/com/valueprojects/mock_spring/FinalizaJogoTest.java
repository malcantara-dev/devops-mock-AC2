package br.com.valueprojects.mock_spring;




import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.FinalizaJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;
import infra.EnviadorDeSms;
import infra.JogoDao;




public class FinalizaJogoTest {

	private Calendar antiga;
	private Participante joao;
	private Participante maria;
	private JogoDao daoFalso;
	private EnviadorDeSms sms;

	@BeforeEach
	public void setUp() {
		antiga = Calendar.getInstance();
		antiga.set(1999, 1, 20);

		joao = new Participante(1, "Joao");
		maria = new Participante(2, "Maria");

		// mocks no lugar do DAO real (MySQL) e do gateway de SMS real
		daoFalso = mock(JogoDao.class);
		sms = mock(EnviadorDeSms.class);
	}

	 @Test
	    public void deveFinalizarJogosDaSemanaAnterior() {

	        Jogo jogo1 = new CriadorDeJogo().para("Caça moedas")
	            .naData(antiga).constroi();
	        Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras")
	            .naData(antiga).constroi();

	        // stub: estado pronto devolvido pelo DAO
	        List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);
	        when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

	        FinalizaJogo finalizador = new FinalizaJogo(daoFalso, sms);
	        finalizador.finaliza();

	        assertTrue(jogo1.isFinalizado());
	        assertTrue(jogo2.isFinalizado());
	        assertEquals(2, finalizador.getTotalFinalizados());
	    }

	 @Test
		public void deveVerificarSeMetodoAtualizaFoiInvocado() {

			Jogo jogo1 = new CriadorDeJogo().para("Cata moedas").naData(antiga).constroi();
			Jogo jogo2 = new CriadorDeJogo().para("Derruba barreiras").naData(antiga).constroi();

			List<Jogo> jogosAnteriores = Arrays.asList(jogo1, jogo2);
			when(daoFalso.emAndamento()).thenReturn(jogosAnteriores);

			FinalizaJogo finalizador = new FinalizaJogo(daoFalso, sms);
			finalizador.finaliza();

			// mock: verifica o comportamento (a interação), não o estado
			verify(daoFalso, times(1)).atualiza(jogo1);
			verify(daoFalso, times(1)).atualiza(jogo2);
		}

	 @Test
		public void deveSalvarOJogoAntesDeEnviarOSmsAoVencedor() {

			Jogo jogo = new CriadorDeJogo().para("Cata moedas").naData(antiga)
				.resultado(joao, 150.0)
				.resultado(maria, 200.0)
				.constroi();
			when(daoFalso.emAndamento()).thenReturn(List.of(jogo));

			new FinalizaJogo(daoFalso, sms).finaliza();

			// a ordem é a regra de negócio: primeiro persiste, depois notifica
			InOrder ordem = inOrder(daoFalso, sms);
			ordem.verify(daoFalso).atualiza(jogo);
			ordem.verify(sms).envia(eq(maria), anyString());
			ordem.verifyNoMoreInteractions();
		}

	 @Test
		public void deveSalvarOJogoFinalizadoJaComOVencedorIdentificado() {

			Jogo jogo = new CriadorDeJogo().para("Cata moedas").naData(antiga)
				.resultado(joao, 300.0)
				.resultado(maria, 200.0)
				.constroi();
			when(daoFalso.emAndamento()).thenReturn(List.of(jogo));

			new FinalizaJogo(daoFalso, sms).finaliza();

			ArgumentCaptor<Jogo> salvo = ArgumentCaptor.forClass(Jogo.class);
			verify(daoFalso).atualiza(salvo.capture());
			assertTrue(salvo.getValue().isFinalizado());
			assertEquals(joao, salvo.getValue().getVencedor());
		}

	 @Test
		public void deveEnviarAoVencedorUmaMensagemInformandoAVitoria() {

			Jogo jogo = new CriadorDeJogo().para("Derruba barreiras").naData(antiga)
				.resultado(joao, 90.0)
				.resultado(maria, 94.0)
				.constroi();
			when(daoFalso.emAndamento()).thenReturn(List.of(jogo));

			new FinalizaJogo(daoFalso, sms).finaliza();

			ArgumentCaptor<Participante> destinatario = ArgumentCaptor.forClass(Participante.class);
			ArgumentCaptor<String> mensagem = ArgumentCaptor.forClass(String.class);
			verify(sms).envia(destinatario.capture(), mensagem.capture());
			assertSame(maria, destinatario.getValue());
			assertEquals("Parabéns, Maria! Você venceu o jogo Derruba barreiras.", mensagem.getValue());
		}

	 @Test
		public void naoDeveEnviarSmsQuandoFalharAoSalvarOJogo() {

			Jogo jogoQueFalha = new CriadorDeJogo().para("Cata moedas").naData(antiga)
				.resultado(joao, 150.0)
				.constroi();
			Jogo jogoQueSalva = new CriadorDeJogo().para("Derruba barreiras").naData(antiga)
				.resultado(maria, 200.0)
				.constroi();
			when(daoFalso.emAndamento()).thenReturn(Arrays.asList(jogoQueFalha, jogoQueSalva));
			// stub de falha: simula queda do banco só para o primeiro jogo
			doThrow(new RuntimeException("banco indisponível")).when(daoFalso).atualiza(jogoQueFalha);

			FinalizaJogo finalizador = new FinalizaJogo(daoFalso, sms);
			finalizador.finaliza();

			verify(sms, never()).envia(eq(joao), anyString());
			verify(sms, times(1)).envia(eq(maria), anyString());
			assertEquals(1, finalizador.getTotalFinalizados());
		}

	 @Test
		public void naoDeveFinalizarNemNotificarJogosDaSemanaCorrente() {

			Calendar ontem = Calendar.getInstance();
			ontem.add(Calendar.DAY_OF_MONTH, -1);
			Jogo jogo = new CriadorDeJogo().para("Cata moedas").naData(ontem)
				.resultado(joao, 150.0)
				.constroi();
			when(daoFalso.emAndamento()).thenReturn(List.of(jogo));

			FinalizaJogo finalizador = new FinalizaJogo(daoFalso, sms);
			finalizador.finaliza();

			assertFalse(jogo.isFinalizado());
			assertEquals(0, finalizador.getTotalFinalizados());
			verify(daoFalso, never()).atualiza(any(Jogo.class));
			verifyNoInteractions(sms);
		}

	 @Test
		public void naoDeveEnviarSmsQuandoOJogoNaoTemVencedor() {

			Jogo semResultados = new CriadorDeJogo().para("Cata moedas").naData(antiga).constroi();
			when(daoFalso.emAndamento()).thenReturn(List.of(semResultados));

			new FinalizaJogo(daoFalso, sms).finaliza();

			assertTrue(semResultados.isFinalizado());
			assertNull(semResultados.getVencedor());
			verify(daoFalso).atualiza(semResultados);
			verifyNoInteractions(sms);
		}

	 @Test
		public void naoDeveInteragirComSmsQuandoNaoHaJogosEmAndamento() {

			when(daoFalso.emAndamento()).thenReturn(Collections.emptyList());

			new FinalizaJogo(daoFalso, sms).finaliza();

			verify(daoFalso).emAndamento();
			verifyNoMoreInteractions(daoFalso);
			verifyNoInteractions(sms);
		}

	}
