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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TesteDriveServiceTest {

    @Mock
    private TesteDriveRepository testeDriveRepository;

    @Mock
    private EnviarTesteDrive enviarTesteDrive;

    @InjectMocks
    private TesteDriveService testeDriveService;

    @Test
    void testSalvarTesteDriveEnviaRabbitMQ() {
        TesteDrive td = new TesteDrive("Carlos", "999", "111", "000", 2L, "Polo (2023)", LocalDateTime.now(), "Ana", "Agendado");
        TesteDrive salvo = new TesteDrive("Carlos", "999", "111", "000", 2L, "Polo (2023)", LocalDateTime.now(), "Ana", "Agendado");
        salvo.setId(7L);

        when(testeDriveRepository.save(any(TesteDrive.class))).thenReturn(salvo);

        TesteDrive res = testeDriveService.save(td, 4L, "Ana Vendedora", "VENDEDOR");

        assertNotNull(res);
        assertEquals(7L, res.getId());

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarTesteDrive).enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();
        assertEquals("TESTE_DRIVE", log.getEntidade());
        assertEquals("CADASTRO", log.getTipoOperacao());
        assertEquals(7L, log.getReferenciaId());
    }
}
