
package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Primary
@Component("enviar")
public class Enviar {

    // Configurações pelas variáveis de ambiente
    public static final boolean ENABLED =
            Boolean.parseBoolean(
                    System.getenv().getOrDefault(
                            "RABBITMQ_ENABLED", "false"
                    )
            );

    public static final String HOST =
            System.getenv().getOrDefault(
                    "RABBITMQ_HOST", "localhost"
            );

    public static final int PORT = 5672;

    public static final String USER =
            System.getenv().getOrDefault(
                    "RABBITMQ_USER", ""
            );

    public static final String PASS =
            System.getenv().getOrDefault(
                    "RABBITMQ_PASSWORD", ""
            );

    public static final String DEFAULT_QUEUE =
            System.getenv().getOrDefault(
                    "RABBITMQ_QUEUE", "brunno"
            );

    protected static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule());

    // Envio direto de mensagens ao RabbitMQ
    public static void enviarMensagem(
            String message,
            String queue
    ) {

        // Permite executar a aplicação sem RabbitMQ
        if (!ENABLED) {

            System.out.println(
                    "[AUDITORIA] RabbitMQ desativado."
            );

            return;
        }

        // Valida as credenciais
        if (USER.isBlank() || PASS.isBlank()) {

            System.err.println(
                    "[RABBITMQ] Credenciais não configuradas."
            );

            return;
        }

        if (message == null || message.isBlank()) {
            return;
        }

        String fila = queue == null || queue.isBlank()
                ? DEFAULT_QUEUE
                : queue;

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USER);
        factory.setPassword(PASS);

        // Evita longas esperas quando o servidor está offline
        factory.setConnectionTimeout(3000);
        factory.setHandshakeTimeout(3000);

        try (
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()
        ) {

            channel.queueDeclare(
                    fila,
                    true,
                    false,
                    false,
                    null
            );

            channel.confirmSelect();

            channel.basicPublish(
                    "",
                    fila,
                    MessageProperties.PERSISTENT_BASIC,
                    message.getBytes(StandardCharsets.UTF_8)
            );

            // Aguarda confirmação do broker
            channel.waitForConfirmsOrDie(3000);

            System.out.println(
                    "[RABBITMQ] Mensagem publicada na fila: "
                            + fila
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "[RABBITMQ] Envio interrompido."
            );

        } catch (Exception e) {

            System.err.println(
                    "[RABBITMQ] Falha na comunicação com: "
                            + HOST
            );
        }
    }

    // Envia o DTO para a fila padrão
    public void enviar(LogOperacaoDTO logDTO) {

        enviar(logDTO, DEFAULT_QUEUE);
    }

    // Envia o DTO para uma fila específica
    public void enviar(
            LogOperacaoDTO logDTO,
            String queue
    ) {

        enviarMensagemDTO(logDTO, queue);
    }

    // Envio de auditoria de qualquer entidade
    public void enviarLogEntidade(
            String entidade,
            String tipoOperacao,
            Long referenciaId,
            Double valor,
            Long usuarioId,
            String usuarioNome,
            String usuarioCargo,
            String detalhes
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                entidade,
                tipoOperacao,
                referenciaId,
                valor,
                usuarioId,
                usuarioNome,
                usuarioCargo,
                detalhes
        );

        enviar(dto);
    }

    // Auditoria de veículos
    public static void enviarLogCarro(
            Long id,
            String modelo,
            Integer ano,
            Double preco,
            String operacao,
            String usuario
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                operacao,
                id,
                modelo,
                ano,
                preco,
                1L,
                usuario != null ? usuario : "Sistema",
                "ADMIN",
                String.format(
                        "Carro %s: %s (%d) por R$ %.2f",
                        operacao,
                        modelo,
                        ano,
                        preco
                )
        );

        dto.setEntidade("CARRO");

        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    // Auditoria de test drives
    public static void enviarLogTesteDrive(
            Long id,
            String cliente,
            String modelo,
            String dataHora,
            String status,
            String operacao,
            String responsavel
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                operacao,
                id,
                0.0,
                1L,
                responsavel != null
                        ? responsavel : "Sistema",
                "ADMIN",
                String.format(
                        "Test Drive %s: Cliente %s com %s (%s) - Data: %s",
                        operacao,
                        cliente,
                        modelo,
                        status,
                        dataHora
                )
        );

        dto.setCarroModelo(modelo);

        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    // Auditoria de usuários
    public static void enviarLogUsuario(
            Long id,
            String nome,
            String cargo,
            String login,
            String operacao,
            String usuario
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                operacao,
                id,
                0.0,
                1L,
                usuario != null ? usuario : "Sistema",
                "ADMIN",
                String.format(
                        "Usuário %s: %s (Perfil: %s)",
                        operacao,
                        nome,
                        cargo
                )
        );

        enviarMensagemDTO(dto, DEFAULT_QUEUE);
    }

    // Método legado mantido para compatibilidade
    @Deprecated
    public static void enviarLogVenda(
            Long id,
            String modelo,
            String cliente,
            Double valor,
            String formaPagamento,
            String operacao,
            String vendedor
    ) {

        System.out.println(
                "[AUDITORIA] Módulo de vendas desativado."
        );
    }

    // Método legado mantido para compatibilidade
    @Deprecated
    public static void enviarLogContabil(
            Long id,
            String descricao,
            String tipo,
            Double valor,
            String categoria,
            String operacao,
            String usuario
    ) {

        System.out.println(
                "[AUDITORIA] Módulo contábil desativado."
        );
    }

    // Converte o DTO para JSON
    private static void enviarMensagemDTO(
            LogOperacaoDTO dto,
            String queue
    ) {

        if (dto == null) {
            return;
        }

        try {

            String json =
                    objectMapper.writeValueAsString(dto);

            enviarMensagem(json, queue);

        } catch (Exception e) {

            System.err.println(
                    "[RABBITMQ] Erro ao serializar DTO."
            );
        }
    }

    // Envio de texto direto
    public void enviar(String mensagem) {

        enviarMensagem(mensagem, DEFAULT_QUEUE);
    }

    // Verificação isolada da configuração
    public static void main(String[] args) {

        System.out.println(
                "RabbitMQ habilitado: " + ENABLED
        );

        System.out.println(
                "Servidor: " + HOST
        );

        System.out.println(
                "Fila: " + DEFAULT_QUEUE
        );
    }
}
