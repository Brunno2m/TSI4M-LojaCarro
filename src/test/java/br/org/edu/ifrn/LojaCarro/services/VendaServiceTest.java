package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarContabil;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarVenda;
import br.org.edu.ifrn.LojaCarro.model.Contabil;
import br.org.edu.ifrn.LojaCarro.model.Venda;
import br.org.edu.ifrn.LojaCarro.repository.ContabilRepository;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private ContabilRepository contabilRepository;

    @Mock
    private EnviarVenda enviarVenda;

    @Mock
    private EnviarContabil enviarContabil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VendaService vendaService;

    @Test
    void testSalvarVendaPorVendedorGeraComissaoDeTresPorcentoEEmitelogs() {
        Venda venda = new Venda(1L, "Civic (2024)", "Lucas", "111", "999", 150000.0, "PIX", 4L, "Ana Vendedora", "À vista");
        Venda salva = new Venda(1L, "Civic (2024)", "Lucas", "111", "999", 150000.0, "PIX", 4L, "Ana Vendedora", "À vista");
        salva.setId(10L);

        when(vendaRepository.save(any(Venda.class))).thenReturn(salva);
        when(contabilRepository.save(any(Contabil.class))).thenAnswer(i -> {
            Contabil c = i.getArgument(0);
            c.setId(101L);
            return c;
        });

        Venda resultado = vendaService.save(venda, 4L, "Ana Vendedora", "VENDEDOR");

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());

        // Verifica que lançou 2 registros no contábil: ENTRADA (venda) e SAÍDA (comissão 3%)
        ArgumentCaptor<Contabil> captorContabil = ArgumentCaptor.forClass(Contabil.class);
        verify(contabilRepository, times(2)).save(captorContabil.capture());

        List<Contabil> lancamentos = captorContabil.getAllValues();
        Contabil entrada = lancamentos.get(0);
        assertEquals("ENTRADA", entrada.getTipo());
        assertEquals("VENDA", entrada.getCategoria());
        assertEquals(150000.0, entrada.getValor());

        Contabil comissao = lancamentos.get(1);
        assertEquals("SAIDA", comissao.getTipo());
        assertEquals("COMISSAO", comissao.getCategoria());
        assertEquals(4500.0, comissao.getValor()); // 3% de 150.000 = 4.500

        // Verifica envio ao RabbitMQ do log de VENDAS
        ArgumentCaptor<LogOperacaoDTO> captorVenda = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarVenda).enviar(captorVenda.capture());

        LogOperacaoDTO logVenda = captorVenda.getValue();
        assertEquals("VENDAS", logVenda.getEntidade());
        assertEquals("CADASTRO", logVenda.getTipoOperacao());
        assertEquals(150000.0, logVenda.getValor());
        assertEquals(3.0, logVenda.getPercentualComissao());
        assertEquals(4500.0, logVenda.getValorComissao());
        assertEquals("Ana Vendedora", logVenda.getUsuarioNome());
        assertEquals("VENDEDOR", logVenda.getUsuarioCargo());

        // Verifica envio ao RabbitMQ do log CONTÁBIL da comissão
        ArgumentCaptor<LogOperacaoDTO> captorComissao = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarContabil).enviar(captorComissao.capture());

        LogOperacaoDTO logComissao = captorComissao.getValue();
        assertEquals("CONTABIL", logComissao.getEntidade());
        assertEquals(4500.0, logComissao.getValor());
        assertEquals(3.0, logComissao.getPercentualComissao());
    }

    @Test
    void testSalvarVendaPorGerenteGeraComissaoDeDoisPorcento() {
        Venda venda = new Venda(2L, "Corolla", "Maria", "222", "888", 100000.0, "PIX", 3L, "Carlos Gerente", "");
        Venda salva = new Venda(2L, "Corolla", "Maria", "222", "888", 100000.0, "PIX", 3L, "Carlos Gerente", "");
        salva.setId(20L);

        when(vendaRepository.save(any(Venda.class))).thenReturn(salva);
        when(contabilRepository.save(any(Contabil.class))).thenAnswer(i -> {
            Contabil c = i.getArgument(0);
            c.setId(201L);
            return c;
        });

        vendaService.save(venda, 3L, "Carlos Gerente", "GERENTE");

        ArgumentCaptor<Contabil> captorContabil = ArgumentCaptor.forClass(Contabil.class);
        verify(contabilRepository, times(2)).save(captorContabil.capture());

        Contabil comissao = captorContabil.getAllValues().get(1);
        assertEquals("SAIDA", comissao.getTipo());
        assertEquals("COMISSAO", comissao.getCategoria());
        assertEquals(2000.0, comissao.getValor()); // 2% de 100.000 = 2.000
    }

    @Test
    void testSalvarVendaPorSuperusuarioGeraComissaoDeUmPorcento() {
        Venda venda = new Venda(3L, "Polo", "Jose", "333", "777", 80000.0, "PIX", 2L, "Admin Geral", "");
        Venda salva = new Venda(3L, "Polo", "Jose", "333", "777", 80000.0, "PIX", 2L, "Admin Geral", "");
        salva.setId(30L);

        when(vendaRepository.save(any(Venda.class))).thenReturn(salva);
        when(contabilRepository.save(any(Contabil.class))).thenAnswer(i -> {
            Contabil c = i.getArgument(0);
            c.setId(301L);
            return c;
        });

        vendaService.save(venda, 2L, "Admin Geral", "SUPERUSUARIO");

        ArgumentCaptor<Contabil> captorContabil = ArgumentCaptor.forClass(Contabil.class);
        verify(contabilRepository, times(2)).save(captorContabil.capture());

        Contabil comissao = captorContabil.getAllValues().get(1);
        assertEquals("SAIDA", comissao.getTipo());
        assertEquals("COMISSAO", comissao.getCategoria());
        assertEquals(800.0, comissao.getValor()); // 1% de 80.000 = 800
    }

    @Test
    void testSalvarVendaPorProprietarioNaoGeraComissao() {
        Venda venda = new Venda(4L, "Tracker", "Pedro", "444", "666", 120000.0, "PIX", 1L, "Carlos Proprietario", "");
        Venda salva = new Venda(4L, "Tracker", "Pedro", "444", "666", 120000.0, "PIX", 1L, "Carlos Proprietario", "");
        salva.setId(40L);

        when(vendaRepository.save(any(Venda.class))).thenReturn(salva);

        vendaService.save(venda, 1L, "Carlos Proprietario", "PROPRIETARIO");

        // Apenas ENTRADA (nenhuma SAÍDA de comissão)
        verify(contabilRepository, times(1)).save(any(Contabil.class));
        verify(enviarContabil, never()).enviar(any(LogOperacaoDTO.class));
    }

    @Test
    void testDeletarVendaEnviaRabbitMQ() {
        Venda venda = new Venda(1L, "Polo (2023)", "Lucas", "111", "999", 78000.0, "PIX", 4L, "Ana Vendedora", "");
        venda.setId(5L);

        when(vendaRepository.findById(5L)).thenReturn(Optional.of(venda));

        vendaService.deleteById(5L, 4L, "Ana Vendedora", "VENDEDOR");

        verify(vendaRepository).deleteById(5L);

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarVenda).enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();
        assertEquals("VENDAS", log.getEntidade());
        assertEquals("EXCLUSAO", log.getTipoOperacao());
        assertEquals(5L, log.getReferenciaId());
    }
}
