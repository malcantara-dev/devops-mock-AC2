package infra;

import br.com.valueprojects.mock_spring.model.Participante;

/**
 * Porta de saída para o gateway de SMS (Twilio, Zenvia, AWS SNS...).
 * Nos testes é substituída por um mock: nenhum SMS real é enviado.
 */
public interface EnviadorDeSms {

	void envia(Participante destinatario, String mensagem);
}
