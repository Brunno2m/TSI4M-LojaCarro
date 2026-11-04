package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarContabil;
import br.org.edu.ifrn.LojaCarro.model.Contabil;
import br.org.edu.ifrn.LojaCarro.repository.ContabilRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContabilServiceTest {

    @Mock
    private ContabilRepository contabilRepository;

    @Mock
    private EnviarContabil enviarContabil;

    @InjectMocks
    private ContabilService contabilService;

    @Test
    void testSalvarContabilEnviaMensagemRabbitMQ() {
        Contabil entrada = new Contabil("Venda de Corolla", "ENTRADA", 120000.0, "VENDA", "Roberto", "Venda quitada");
        Contabil salvo = new Contabil("Venda de Corolla", "ENTRADA", 120000.0, "VENDA", "Roberto", "Venda quitada");
        salvo.setId(1L);

        when(contabilRepository.save(any(Contabil.class))).thenReturn(salvo);

        Contabil resultado = contabilService.save(entrada, 5L, "Roberto Contador", "CONTABIL");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarContabil).enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();
        assertEquals("CONTABIL", log.getEntidade());
        assertEquals("CADASTRO", log.getTipoOperacao());
        assertEquals(120000.0, log.getValor());
        assertEquals("Roberto Contador", log.getUsuarioNome());
    }

    @Test
    void testCalcularTotaisESaldo() {
        Contabil c1 = new Contabil("Entrada 1", "ENTRADA", 1000.0, "VENDA", "A", "");
        Contabil c2 = new Contabil("Entrada 2", "ENTRADA", 2000.0, "INVESTIMENTO", "A", "");
        Contabil c3 = new Contabil("Saida 1", "SAIDA", 500.0, "DESPESA_FIXA", "A", "");

        when(contabilRepository.findAll()).thenReturn(Arrays.asList(c1, c2, c3));

        assertEquals(3000.0, contabilService.calcularTotalEntradas());
        assertEquals(500.0, contabilService.calcularTotalSaidas());
        assertEquals(2500.0, contabilService.calcularSaldoAtual());
    }

    @Test
    void testDeletarContabilEnviaMensagemRabbitMQ() {
        Contabil c = new Contabil("Despesa Luz", "SAIDA", 300.0, "DESPESA_FIXA", "Roberto", "");
        c.setId(2L);

        when(contabilRepository.findById(2L)).thenReturn(Optional.of(c));

        contabilService.deleteById(2L, 5L, "Roberto Contador", "CONTABIL");

        verify(contabilRepository).deleteById(2L);

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarContabil).enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();
        assertEquals("CONTABIL", log.getEntidade());
        assertEquals("EXCLUSAO", log.getTipoOperacao());
        assertEquals(2L, log.getReferenciaId());
    }
}
