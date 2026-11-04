package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnviarTest {

    @Test
    void testConstantesConfiguracaoRabbitMQ() {
        assertEquals("15.228.215.36", Enviar.HOST);
        assertEquals(5672, Enviar.PORT);
        assertEquals("aluno", Enviar.USER);
        assertEquals("12345", Enviar.PASS);
        assertEquals("guilherme_brunno", Enviar.DEFAULT_QUEUE);
        assertEquals("guilherme_brunno", Receber.QUEUE_NAME);
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
                "Carlos Gerente",
                "GERENTE",
                "Teste unitário"
        );
        assertNotNull(dto);
        assertEquals("CADASTRO", dto.getTipoOperacao());
    }
}
