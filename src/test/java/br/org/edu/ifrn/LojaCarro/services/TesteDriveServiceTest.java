
package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarTesteDrive;
import br.org.edu.ifrn.LojaCarro.model.TesteDrive;
import br.org.edu.ifrn.LojaCarro.repository.TesteDriveRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TesteDriveServiceTest {

    @Mock
    private TesteDriveRepository testeDriveRepository;

    @Mock
    private EnviarTesteDrive enviarTesteDrive;

    @InjectMocks
    private TesteDriveService testeDriveService;

    // Teste de cadastro e envio do evento de auditoria
    @Test
    void testSalvarTesteDriveEnviaRabbitMQ() {

        LocalDateTime horario = LocalDateTime.now()
                .plusDays(1)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        TesteDrive testeDrive = new TesteDrive(
                "Carlos",
                "999",
                "111",
                "000",
                2L,
                "Polo (2023)",
                horario,
                "Carlos",
                "CONFIRMADO"
        );

        // Simula o armazenamento no banco de dados
        when(testeDriveRepository.save(any(TesteDrive.class)))
                .thenAnswer(invocation -> {

                    TesteDrive agendamento =
                            invocation.getArgument(0);

                    agendamento.setId(7L);

                    return agendamento;
                });

        // Executa a operacao
        TesteDrive resultado = testeDriveService.save(
                testeDrive,
                4L,
                "Carlos",
                "CLIENTE"
        );

        // Verifica o resultado do agendamento
        assertNotNull(resultado);
        assertEquals(7L, resultado.getId());

        // Verifica a persistencia
        verify(testeDriveRepository, times(1))
                .save(any(TesteDrive.class));

        // Captura o evento enviado para auditoria
        ArgumentCaptor<LogOperacaoDTO> captor =
                ArgumentCaptor.forClass(LogOperacaoDTO.class);

        verify(enviarTesteDrive, times(1))
                .enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();

        // Valida os dados do evento
        assertNotNull(log);
        assertEquals("TESTE_DRIVE", log.getEntidade());
        assertEquals("CADASTRO", log.getTipoOperacao());
        assertEquals(7L, log.getReferenciaId());
        assertEquals(4L, log.getUsuarioId());
        assertEquals("Carlos", log.getUsuarioNome());
        assertEquals("CLIENTE", log.getUsuarioCargo());
    }
}
