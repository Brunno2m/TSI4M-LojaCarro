package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Primary
@Component("enviar")
public class Enviar {

    // Configurações do broker RabbitMQ informado pelo professor (IP: 15.228.215.36)
    public static final String HOST = "15.228.215.36";
    public static final int PORT = 5672;
    public static final String USER = "aluno";
    public static final String PASS = "12345";
    public static final String DEFAULT_QUEUE = "brunno";

    protected static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Ponto de entrada para testes diretos de envio isolado para cada CRUD.
     */
    public static void main(String[] args) {
        System.out.println("=== TESTE DE ENVIO INDIVIDUAL POR CRUD (RabbitMQ: " + HOST + ") ===");

        // 1. Envio separado para o CRUD de Carro
        enviarLogCarro(1L, "Corolla XEi", 2024, 148000.0, "CADASTRO", "Carlos Gerente");

        // 2. Envio separado para o CRUD de Vendas
        enviarLogVenda(10L, "Civic Touring", "Marcos Silva", 165000.0, "PIX", "CADASTRO", "Ana Vendedora");

        // 3. Envio separado para o CRUD Contábil
        enviarLogContabil(5L, "Recebimento Entrada Venda Veículo", "ENTRADA", 50000.0, "VENDA", "CADASTRO", "Roberto Contador");

        // 4. Envio separado para o CRUD de Test Drive
        enviarLogTesteDrive(3L, "Juliana Costa", "Polo Track (2024)", "2026-11-05 14:00:00", "AGENDADO", "CADASTRO", "Ana Vendedora");

        // 5. Envio separado para o CRUD de Usuário
        enviarLogUsuario(8L, "Mariana Consultora", "VENDEDOR", "mariana", "CADASTRO", "Admin");
    }

    /**
     * Método estático para envio de mensagem direta na fila RabbitMQ.
     *
     * @param message Mensagem (texto ou JSON)
     * @param queue   Nome da fila (ex: "guilherme_brunno")
     */
    public static void enviarMensagem(String message, String queue) {
        ConnectionFactory factory = new ConnectionFactory();

        try {
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername(USER);
            factory.setPassword(PASS);
        } catch (Exception e) {
            System.out.println("Erro ao configurar conexão: " + e.getMessage());
            e.printStackTrace();
        }

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(queue, true, false, false, null);
            channel.basicPublish("", queue, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("MENSAGEM ENVIADA para [" + queue + "] = '" + message + "'");
        } catch (Exception e) {
            System.out.println("Erro ao enviar mensagem para RabbitMQ (" + HOST + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envia o DTO completo da operação convertido para JSON para a fila padrão.
     *
     * @param logDTO Objeto contendo os dados da operação
     */
    public void enviar(LogOperacaoDTO logDTO) {
        enviar(logDTO, DEFAULT_QUEUE);
    }

    /**
     * Envia o DTO para uma fila específica (permite filas separadas por CRUD caso desejado).
     */
    public void enviar(LogOperacaoDTO logDTO, String queue) {
        try {
            String json = objectMapper.writeValueAsString(logDTO);
            enviarMensagem(json, queue != null ? queue : DEFAULT_QUEUE);
        } catch (Exception e) {
            System.out.println("Erro ao serializar DTO para JSON: " + e.getMessage());
        }
    }

    /**
     * Helper para envio de log de qualquer entidade (CONTABIL, TESTE_DRIVE, USUARIO, CARRO, VENDAS).
     */
    public void enviarLogEntidade(String entidade, String tipoOperacao, Long referenciaId,
                                  Double valor, Long usuarioId, String usuarioNome,
                                  String usuarioCargo, String detalhes) {
        LogOperacaoDTO dto = new LogOperacaoDTO(entidade, tipoOperacao, referenciaId, valor, usuarioId, usuarioNome, usuarioCargo, detalhes);
        enviar(dto);
    }

    /**
     * 1. Envio isolado de log para o CRUD de CARRO
     */
    public static void enviarLogCarro(Long id, String modelo, Integer ano, Double preco, String operacao, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                operacao,
                id,
                modelo,
                ano,
                preco,
                1L,
                usuario != null ? usuario : "Sistema",
                "ESTOQUE",
                String.format("Carro %s: %s (%d) por R$ %.2f", operacao, modelo, ano, preco)
        );
        dto.setEntidade("CARRO");
        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    /**
     * 2. Envio isolado de log para o CRUD de VENDAS
     */
    public static void enviarLogVenda(Long id, String modelo, String cliente, Double valor, String formaPagamento, String operacao, String vendedor) {
        double taxa = 0.03; // Vendedor: 3%
        double comissao = Math.round(((valor != null ? valor : 0.0) * taxa) * 100.0) / 100.0;
        String detalhes = String.format("Venda %s: %s para %s por R$ %.2f (%s) | Vendedor: %s (VENDEDOR) | Comissão (3.0%%): R$ %.2f",
                operacao, modelo, cliente, valor, formaPagamento, vendedor != null ? vendedor : "Vendedor", comissao);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "VENDAS",
                operacao,
                id,
                valor,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                "VENDEDOR",
                detalhes
        );
        dto.setCarroModelo(modelo);
        dto.setPercentualComissao(3.0);
        dto.setValorComissao(comissao);
        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    /**
     * 3. Envio isolado de log para o CRUD CONTÁBIL
     */
    public static void enviarLogContabil(Long id, String descricao, String tipo, Double valor, String categoria, String operacao, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CONTABIL",
                operacao,
                id,
                valor,
                1L,
                usuario != null ? usuario : "Contador",
                "CONTABIL",
                String.format("Lançamento Contábil %s (%s): %s | R$ %.2f [%s]", operacao, tipo, descricao, valor, categoria)
        );
        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    /**
     * 4. Envio isolado de log para o CRUD de TESTE DRIVE
     */
    public static void enviarLogTesteDrive(Long id, String cliente, String modelo, String dataHora, String status, String operacao, String vendedor) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                operacao,
                id,
                0.0,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                "VENDEDOR",
                String.format("Test Drive %s: Cliente %s com %s (%s) - Data: %s", operacao, cliente, modelo, status, dataHora)
        );
        dto.setCarroModelo(modelo);
        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    /**
     * 5. Envio isolado de log para o CRUD de USUÁRIO
     */
    public static void enviarLogUsuario(Long id, String nome, String cargo, String login, String operacao, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                operacao,
                id,
                0.0,
                1L,
                usuario != null ? usuario : "Admin",
                "SUPERUSUARIO",
                String.format("Usuário %s: %s (Cargo: %s, Login: %s)", operacao, nome, cargo, login)
        );
        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    private static void enviarMensagemDTO(LogOperacaoDTO dto, String queue) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            enviarMensagem(json, queue);
        } catch (Exception e) {
            System.out.println("Erro ao converter DTO para JSON: " + e.getMessage());
        }
    }

    /**
     * Sobrecarga de conveniência para envio de texto direto.
     */
    public void enviar(String mensagem) {
        enviarMensagem(mensagem, DEFAULT_QUEUE);
    }
}
