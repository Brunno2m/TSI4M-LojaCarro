
package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnviarTest {

    @Test
    void testConstantesConfiguracaoRabbitMQ() {

        assertEquals(
                System.getenv().getOrDefault(
                        "RABBITMQ_HOST", "localhost"
                ),
                Enviar.HOST
        );

        assertEquals(5672, Enviar.PORT);

        assertEquals(
                System.getenv().getOrDefault(
                        "RABBITMQ_USER", ""
                ),
                Enviar.USER
        );

        assertTrue(
                System.getenv()
                        .getOrDefault("RABBITMQ_PASSWORD", "")
                        .equals(Enviar.PASS),
                "Configuracao da senha RabbitMQ incorreta."
        );

        assertEquals(
                System.getenv().getOrDefault(
                        "RABBITMQ_QUEUE", "brunno"
                ),
                Enviar.DEFAULT_QUEUE
        );

        assertEquals(
                System.getenv().getOrDefault(
                        "RABBITMQ_RECEIVE_QUEUE",
                        "guilherme_brunno"
                ),
                Receber.QUEUE_NAME
        );

        assertEquals(
                Boolean.parseBoolean(
                        System.getenv().getOrDefault(
                                "RABBITMQ_ENABLED", "false"
                        )
                ),
                Enviar.ENABLED
        );
    }

    @Test
    void testInstanciacaoEnviar() {

        Enviar enviar = new Enviar();

        assertNotNull(enviar);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CADASTRO",
                1L,
                "Civic",
                2024,
                150000.0,
                2L,
                "Administrador Teste",
                "ADMIN",
                "Teste unitario"
        );

        dto.setEntidade("CARRO");

        assertNotNull(dto);
        assertEquals("CADASTRO", dto.getTipoOperacao());
        assertEquals("CARRO", dto.getEntidade());
    }
}
