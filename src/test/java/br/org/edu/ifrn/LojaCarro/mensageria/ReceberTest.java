package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceberTest {

    @Mock
    private LogOperacaoRepository logOperacaoRepository;

    private Receber receber;

    @BeforeEach
    void setUp() {
        receber = new Receber();
        receber.setLogOperacaoRepository(logOperacaoRepository);
    }

    @Test
    void testSalvarLogNoBancoViaJson() {
        String json = "{\"tipoOperacao\":\"CADASTRO\",\"carroId\":12,\"carroModelo\":\"Civic\",\"carroAno\":2024,\"carroPreco\":150000.0,\"usuarioId\":2,\"usuarioNome\":\"Carlos\",\"usuarioCargo\":\"GERENTE\",\"detalhes\":\"Operação cadastrada\"}";

        when(logOperacaoRepository.save(any(LogOperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Receber.salvarLogNoBanco(json);

        ArgumentCaptor<LogOperacao> captor = ArgumentCaptor.forClass(LogOperacao.class);
        verify(logOperacaoRepository).save(captor.capture());

        LogOperacao salvo = captor.getValue();
        assertNotNull(salvo);
        assertEquals("CADASTRO", salvo.getTipoOperacao());
        assertEquals(12L, salvo.getCarroId());
        assertEquals("Civic", salvo.getCarroModelo());
        assertEquals(150000.0, salvo.getCarroPreco());
        assertEquals("Carlos", salvo.getUsuarioNome());
    }

    @Test
    void testSalvarLogNoBancoViaTexto() {
        String texto = "Gol,45000.0";

        when(logOperacaoRepository.save(any(LogOperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Receber.salvarLogNoBanco(texto);

        ArgumentCaptor<LogOperacao> captor = ArgumentCaptor.forClass(LogOperacao.class);
        verify(logOperacaoRepository).save(captor.capture());

        LogOperacao salvo = captor.getValue();
        assertNotNull(salvo);
        assertEquals("OPERACAO_TEXTO", salvo.getTipoOperacao());
        assertEquals("Gol", salvo.getCarroModelo());
        assertEquals(45000.0, salvo.getCarroPreco());
    }

    @Test
    void testSalvarLogNoBancoViaJsonAlternativo() {
        String jsonAlt = "{\"operacao\":\"ATUALIZACAO\",\"modelo\":\"Corolla\",\"ano\":2022,\"valor\":110000.0,\"nomeUsuario\":\"Ana\",\"cargo\":\"VENDEDOR\",\"obs\":\"Atualizacao de preco\"}";

        when(logOperacaoRepository.save(any(LogOperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Receber.salvarLogNoBanco(jsonAlt);

        ArgumentCaptor<LogOperacao> captor = ArgumentCaptor.forClass(LogOperacao.class);
        verify(logOperacaoRepository).save(captor.capture());

        LogOperacao salvo = captor.getValue();
        assertNotNull(salvo);
        assertEquals("ATUALIZACAO", salvo.getTipoOperacao());
        assertEquals("Corolla", salvo.getCarroModelo());
        assertEquals(2022, salvo.getCarroAno());
        assertEquals(110000.0, salvo.getCarroPreco());
        assertEquals("Ana", salvo.getUsuarioNome());
        assertEquals("VENDEDOR", salvo.getUsuarioCargo());
        assertEquals("Atualizacao de preco", salvo.getDetalhes());
    }

    @Test
    void testSalvarLogVendaComComissaoExplicita() {
        String json = "{\"entidade\":\"VENDAS\",\"tipoOperacao\":\"CADASTRO\",\"carroModelo\":\"Civic\",\"valor\":100000.0,\"usuarioNome\":\"Ana\",\"usuarioCargo\":\"VENDEDOR\",\"percentualComissao\":3.0,\"valorComissao\":3000.0,\"detalhes\":\"Venda concluida\"}";

        when(logOperacaoRepository.save(any(LogOperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Receber.salvarLogNoBanco(json);

        ArgumentCaptor<LogOperacao> captor = ArgumentCaptor.forClass(LogOperacao.class);
        verify(logOperacaoRepository).save(captor.capture());

        LogOperacao salvo = captor.getValue();
        assertNotNull(salvo);
        assertEquals("VENDAS", salvo.getEntidade());
        assertEquals("CADASTRO", salvo.getTipoOperacao());
        assertEquals(3.0, salvo.getPercentualComissao());
        assertEquals(3000.0, salvo.getValorComissao());
    }

    @Test
    void testSalvarLogVendaCalculaComissaoPorCargoQuandoNaoInformada() {
        String json = "{\"entidade\":\"VENDAS\",\"tipoOperacao\":\"CADASTRO\",\"carroModelo\":\"Corolla\",\"valor\":200000.0,\"usuarioNome\":\"Carlos\",\"usuarioCargo\":\"GERENTE\",\"detalhes\":\"Venda gerente\"}";

        when(logOperacaoRepository.save(any(LogOperacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Receber.salvarLogNoBanco(json);

        ArgumentCaptor<LogOperacao> captor = ArgumentCaptor.forClass(LogOperacao.class);
        verify(logOperacaoRepository).save(captor.capture());

        LogOperacao salvo = captor.getValue();
        assertNotNull(salvo);
        assertEquals(2.0, salvo.getPercentualComissao()); // GERENTE: 2%
        assertEquals(4000.0, salvo.getValorComissao()); // 2% de 200.000 = 4.000
    }

    @Test
    void testSalvarLogNoBancoFallbackJdbc() {
        // Simula execução sem Spring (repositório null)
        receber.setLogOperacaoRepository(null);

        String jsonTeste = "{\"tipoOperacao\":\"TESTE_JDBC\",\"carroModelo\":\"HB20\",\"carroAno\":2021,\"carroPreco\":70000.0,\"usuarioNome\":\"Aluno Teste\",\"detalhes\":\"Gravando via fallback JDBC no MySQL\"}";
        Receber.salvarLogNoBanco(jsonTeste);

        // Restaura para os próximos testes
        receber.setLogOperacaoRepository(logOperacaoRepository);
    }
}
