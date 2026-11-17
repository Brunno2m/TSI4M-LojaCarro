
package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceberTest {

    @Mock
    private LogOperacaoRepository logOperacaoRepository;

    private Receber receber;

    @BeforeEach
    void setUp() {

        receber = new Receber();

        receber.setLogOperacaoRepository(
                logOperacaoRepository
        );
    }

    @AfterEach
    void limpar() {

        receber.setLogOperacaoRepository(null);
    }

    // Metodo auxiliar para capturar o registro de auditoria
    private LogOperacao salvarECapturar(String mensagem) {

        when(logOperacaoRepository.save(any(LogOperacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Receber.salvarLogNoBanco(mensagem);

        ArgumentCaptor<LogOperacao> captor =
                ArgumentCaptor.forClass(LogOperacao.class);

        verify(logOperacaoRepository)
                .save(captor.capture());

        return captor.getValue();
    }

    // Teste 1: Recebimento de JSON
    @Test
    void testSalvarLogNoBancoViaJson() {

        String json = """
                {
                    "entidade": "CARRO",
                    "tipoOperacao": "CADASTRO",
                    "referenciaId": 12,
                    "usuarioId": 2,
                    "usuarioNome": "Administrador",
                    "detalhes": "Veiculo cadastrado"
                }
                """;

        LogOperacao salvo = salvarECapturar(json);

        assertNotNull(salvo);
        assertEquals("CARRO", salvo.getEntidade());
        assertEquals("CADASTRO", salvo.getTipoOperacao());
        assertEquals(12L, salvo.getReferenciaId());
        assertEquals(2L, salvo.getUsuarioId());
        assertEquals("Administrador", salvo.getUsuarioNome());
        assertEquals("Veiculo cadastrado", salvo.getDetalhes());
        assertNotNull(salvo.getDataHora());
    }

    // Teste 2: Recebimento de mensagem de texto
    @Test
    void testSalvarLogNoBancoViaTexto() {

        String mensagem = "Gol,45000.0";

        LogOperacao salvo = salvarECapturar(mensagem);

        assertNotNull(salvo);
        assertEquals("CARRO", salvo.getEntidade());
        assertEquals("OPERACAO_TEXTO", salvo.getTipoOperacao());
        assertEquals("Gol,45000.0", salvo.getDetalhes());
        assertEquals("Dupla RabbitMQ", salvo.getUsuarioNome());
        assertNotNull(salvo.getDataHora());
    }

    // Teste 3: JSON com nomes alternativos
    @Test
    void testSalvarLogNoBancoViaJsonAlternativo() {

        String json = """
                {
                    "modulo": "CARRO",
                    "operacao": "ATUALIZACAO",
                    "carroId": 15,
                    "idUsuario": 3,
                    "nomeUsuario": "Administrador",
                    "obs": "Atualizacao de preco"
                }
                """;

        LogOperacao salvo = salvarECapturar(json);

        assertNotNull(salvo);
        assertEquals("CARRO", salvo.getEntidade());
        assertEquals("ATUALIZACAO", salvo.getTipoOperacao());
        assertEquals(15L, salvo.getReferenciaId());
        assertEquals(3L, salvo.getUsuarioId());
        assertEquals("Administrador", salvo.getUsuarioNome());
        assertEquals("Atualizacao de preco", salvo.getDetalhes());
    }

    // Teste 4: JSON sem campo detalhes
    @Test
    void testSalvarJsonSemDetalhes() {

        String json = """
                {"entidade":"TESTE_DRIVE","acao":"CANCELAMENTO","referenciaId":8}
                """;

        LogOperacao salvo = salvarECapturar(json);

        assertNotNull(salvo);
        assertEquals("TESTE_DRIVE", salvo.getEntidade());
        assertEquals("CANCELAMENTO", salvo.getTipoOperacao());
        assertEquals(8L, salvo.getReferenciaId());
        assertEquals(json.trim(), salvo.getDetalhes());
    }

    // Teste 5: Bloqueio de mensagens invalidas
    @Test
    void testRejeitarMensagemInvalida() {

        assertThrows(
                IllegalArgumentException.class,
                () -> Receber.salvarLogNoBanco("{invalido")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Receber.salvarLogNoBanco("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> Receber.salvarLogNoBanco(null)
        );

        verifyNoInteractions(logOperacaoRepository);
    }

    // Teste 6: Repositorio indisponivel
    @Test
    void testRepositorioIndisponivel() {

        receber.setLogOperacaoRepository(null);

        assertThrows(
                IllegalStateException.class,
                () -> Receber.salvarLogNoBanco("Gol,45000.0")
        );

        verifyNoInteractions(logOperacaoRepository);
    }
}
