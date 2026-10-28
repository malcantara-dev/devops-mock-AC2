package infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.com.valueprojects.mock_spring.builder.CriadorDeJogo;
import br.com.valueprojects.mock_spring.model.Jogo;
import br.com.valueprojects.mock_spring.model.Participante;

/**
 * Teste de unidade do DAO real com JDBC "mockado".
 * Cobre o mapeamento objeto/relacional, mas NÃO prova que o SQL roda no MySQL:
 * para isso servem os testes de integração (ver JogoRepositoryTest).
 */
public class JogoDaoTest {

	private Connection conexao;
	private PreparedStatement ps;
	private PreparedStatement psResultados;
	private ResultSet rs;
	private ResultSet rsResultados;
	private JogoDao dao;

	@BeforeEach
	public void setUp() throws SQLException {
		conexao = mock(Connection.class);
		ps = mock(PreparedStatement.class);
		psResultados = mock(PreparedStatement.class);
		rs = mock(ResultSet.class);
		rsResultados = mock(ResultSet.class);
		dao = new JogoDao(conexao);
	}

	@Test
	public void construtorPadraoDeveAbrirConexaoComOMySql() {
		try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
			driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(conexao);

			new JogoDao();

			driver.verify(() -> DriverManager.getConnection("jdbc:mysql://localhost/mocks", "root", ""));
		}
	}

	@Test
	public void construtorPadraoDeveFalharSemBanco() {
		// criada fora do mockStatic: o construtor de SQLException chama DriverManager.getLogWriter()
		SQLException semBanco = new SQLException("Communications link failure");
		try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
			driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
				.thenThrow(semBanco);

			assertThrows(RuntimeException.class, JogoDao::new);
		}
	}

	@Test
	public void deveSalvarJogoComResultadosUsandoAChaveGerada() throws SQLException {
		Jogo jogo = new CriadorDeJogo().para("Cata moedas").resultado(new Participante(3, "Ana"), 80.0).constroi();
		when(conexao.prepareStatement(startsWith("INSERT INTO JOGO"), anyInt())).thenReturn(ps);
		when(conexao.prepareStatement(startsWith("INSERT INTO RESULTADOS"))).thenReturn(psResultados);
		when(ps.getGeneratedKeys()).thenReturn(rs);
		when(rs.next()).thenReturn(true);
		when(rs.getInt(1)).thenReturn(99);

		dao.salva(jogo);

		assertEquals(99, jogo.getId());
		verify(conexao).prepareStatement(startsWith("INSERT INTO JOGO"), anyInt());
		verify(ps).setString(1, "Cata moedas");
		verify(psResultados).setInt(1, 99);
		verify(psResultados).setInt(2, 3);
		verify(psResultados).setDouble(3, 80.0);
		verify(psResultados).execute();
		verify(ps).close();
	}

	@Test
	public void deveSalvarJogoSemResultadosESemChaveGerada() throws SQLException {
		Jogo jogo = new CriadorDeJogo().para("Cata moedas").comId(5).constroi();
		when(conexao.prepareStatement(anyString(), anyInt())).thenReturn(ps);
		when(ps.getGeneratedKeys()).thenReturn(rs);
		when(rs.next()).thenReturn(false);

		dao.salva(jogo);

		assertEquals(5, jogo.getId());
		verify(conexao).prepareStatement(anyString(), anyInt());
		verify(conexao, never()).prepareStatement(anyString());
	}

	@Test
	public void deveTraduzirFalhaAoSalvar() throws SQLException {
		when(conexao.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("tabela não existe"));

		Jogo jogo = new Jogo("Cata moedas");
		assertThrows(RuntimeException.class, () -> dao.salva(jogo));
	}

	@Test
	public void deveMontarJogosEmAndamentoComSeusResultados() throws SQLException {
		when(conexao.prepareStatement(startsWith("SELECT * FROM JOGO"))).thenReturn(ps);
		when(conexao.prepareStatement(startsWith("SELECT VALOR"))).thenReturn(psResultados);
		when(ps.executeQuery()).thenReturn(rs);
		when(psResultados.executeQuery()).thenReturn(rsResultados);
		when(rs.next()).thenReturn(true, false);
		when(rs.getString("descricao")).thenReturn("Cata moedas");
		when(rs.getDate("data")).thenReturn(java.sql.Date.valueOf("2024-01-10"));
		when(rs.getInt("id")).thenReturn(7);
		when(rs.getBoolean("finalizado")).thenReturn(false);
		when(rsResultados.next()).thenReturn(true, false);
		when(rsResultados.getInt("PARTICIPANTE_ID")).thenReturn(3);
		when(rsResultados.getString("NOME")).thenReturn("Ana");
		when(rsResultados.getDouble("VALOR")).thenReturn(80.0);

		List<Jogo> jogos = dao.emAndamento();

		assertEquals(1, jogos.size());
		Jogo jogo = jogos.get(0);
		assertEquals(7, jogo.getId());
		assertFalse(jogo.isFinalizado());
		assertEquals(new Participante(3, "Ana"), jogo.getResultados().get(0).getParticipante());
		assertEquals(80.0, jogo.getResultados().get(0).getMetrica(), 0.00001);
		verify(conexao).prepareStatement("SELECT * FROM JOGO WHERE FINALIZADO = false;");
	}

	@Test
	public void deveMontarJogosFinalizados() throws SQLException {
		when(conexao.prepareStatement(startsWith("SELECT * FROM JOGO"))).thenReturn(ps);
		when(conexao.prepareStatement(startsWith("SELECT VALOR"))).thenReturn(psResultados);
		when(ps.executeQuery()).thenReturn(rs);
		when(psResultados.executeQuery()).thenReturn(rsResultados);
		when(rs.next()).thenReturn(true, false);
		when(rs.getDate("data")).thenReturn(java.sql.Date.valueOf("2024-01-10"));
		when(rs.getBoolean("finalizado")).thenReturn(true);
		when(rsResultados.next()).thenReturn(false);

		List<Jogo> jogos = dao.finalizados();

		assertTrue(jogos.get(0).isFinalizado());
		verify(conexao).prepareStatement("SELECT * FROM JOGO WHERE FINALIZADO = true;");
	}

	@Test
	public void deveTraduzirFalhaAoConsultar() throws SQLException {
		when(conexao.prepareStatement(anyString())).thenThrow(new SQLException("timeout"));

		assertThrows(RuntimeException.class, () -> dao.emAndamento());
	}

	@Test
	public void deveAtualizarJogoComVencedor() throws SQLException {
		Jogo jogo = new CriadorDeJogo().para("Cata moedas").comId(7).finaliza().constroi();
		jogo.defineVencedor(new Participante(3, "Ana"));
		when(conexao.prepareStatement(startsWith("UPDATE JOGO"))).thenReturn(ps);

		dao.atualiza(jogo);

		verify(ps).setBoolean(3, true);
		verify(ps).setInt(4, 3);
		verify(ps).setInt(5, 7);
		verify(ps).execute();
		verify(ps).close();
	}

	@Test
	public void deveAtualizarJogoSemVencedorGravandoNulo() throws SQLException {
		Jogo jogo = new CriadorDeJogo().para("Cata moedas").comId(7).constroi();
		when(conexao.prepareStatement(startsWith("UPDATE JOGO"))).thenReturn(ps);

		dao.atualiza(jogo);

		verify(ps).setNull(4, Types.INTEGER);
	}

	@Test
	public void deveTraduzirFalhaAoAtualizar() throws SQLException {
		when(conexao.prepareStatement(anyString())).thenThrow(new SQLException("deadlock"));

		Jogo jogo = new Jogo("Cata moedas");
		assertThrows(RuntimeException.class, () -> dao.atualiza(jogo));
	}

	@Test
	public void deveManterOMetodoLegadoX() {
		assertEquals(10, dao.x());
	}

	@Test
	public void deveUsarRetornoDeChaveGeradaNoInsert() throws SQLException {
		when(conexao.prepareStatement(anyString(), anyInt())).thenReturn(ps);
		when(ps.getGeneratedKeys()).thenReturn(rs);

		dao.salva(new Jogo("Cata moedas"));

		verify(conexao).prepareStatement(startsWith("INSERT INTO JOGO"), eq(Statement.RETURN_GENERATED_KEYS));
	}
}
