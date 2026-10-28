package br.com.valueprojects.mock_spring;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

public class MockSpringApplicationMainTest {

	@Test
	public void mainDeveDelegarParaOSpringApplication() {
		String[] args = { "--server.port=0" };

		// mock estático: verifica a chamada sem subir o servidor de verdade
		try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
			Mock_SpringApplication.main(args);

			spring.verify(() -> SpringApplication.run(Mock_SpringApplication.class, args));
		}
	}
}
