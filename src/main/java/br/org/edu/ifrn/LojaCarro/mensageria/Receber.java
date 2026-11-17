package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.LojaCarroApplication;
import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

/** Consumidor de auditoria: utiliza as configuracoes compartilhadas com Enviar. */
@Component
public class Receber {

    private static final Logger logger = LogManager.getLogger(Receber.class);

    public static final boolean ENABLED = Enviar.ENABLED;
    public static final String HOST = Enviar.HOST;
    public static final int PORT = Enviar.PORT;
    public static final String USER = Enviar.USER;
    public static final String PASS = Enviar.PASS;

    // Fila recebida da dupla, diferente da fila padrao de envio.
    public static final String QUEUE_NAME = System.getenv()
            .getOrDefault("RABBITMQ_RECEIVE_QUEUE", "guilherme_brunno");

    // Protocolo legado de teste do professor (opcional e separado da senha do broker).
    private static final String CHALLENGE_TOKEN = System.getenv()
            .getOrDefault("RABBITMQ_CHALLENGE_TOKEN", "");
    private static final String REPLY_QUEUE = System.getenv()
            .getOrDefault("RABBITMQ_REPLY_QUEUE", "");

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static volatile LogOperacaoRepository logRepositoryEstatico;
    private static Connection conexao;
    private static Channel canal;

    @Autowired
    public void setLogOperacaoRepository(LogOperacaoRepository repository) {
        Receber.logRepositoryEstatico = repository;
    }

    // A inicializacao do consumidor passa a acompanhar o backend Spring Boot.
    @EventListener(ApplicationReadyEvent.class)
    public void iniciarConsumidor() {
        try {
            lerMensagem();
        } catch (IOException e) {
            logger.warn("Nao foi possivel iniciar o consumidor RabbitMQ: {}",
                    e.getClass().getSimpleName());
        }
    }

    // Preserva o ponto de entrada antigo, mas utiliza agora o Spring e o JPA.
    public static void main(String[] args) {
        LojaCarroApplication.main(args);
    }

    public static synchronized void lerMensagem() throws IOException {
        if (!ENABLED) {
            logger.info("[AUDITORIA] Consumidor RabbitMQ desativado.");
            return;
        }

        if (HOST.isBlank() || USER.isBlank() || PASS.isBlank() || QUEUE_NAME.isBlank()) {
            logger.warn("[RABBITMQ] Configuracao do consumidor incompleta.");
            return;
        }

        if (logRepositoryEstatico == null) {
            throw new IOException("Repositorio de auditoria nao inicializado pelo Spring.");
        }

        // Evita iniciar dois consumidores da mesma classe no mesmo processo.
        if (conexao != null && conexao.isOpen() && canal != null && canal.isOpen()) {
            return;
        }
        fecharConexao();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setPort(PORT);
        factory.setUsername(USER);
        factory.setPassword(PASS);
        factory.setConnectionTimeout(3000);
        factory.setHandshakeTimeout(3000);

        try {
            conexao = factory.newConnection();
            canal = conexao.createChannel();
            canal.queueDeclare(QUEUE_NAME, true, false, false, null);

            // Preserva a compatibilidade com a exchange direta da dupla.
            try {
                canal.exchangeDeclare(QUEUE_NAME, "direct", true, false, null);
                canal.queueBind(QUEUE_NAME, QUEUE_NAME, "");
                canal.queueBind(QUEUE_NAME, QUEUE_NAME, QUEUE_NAME);
            } catch (IOException e) {
                logger.warn("[RABBITMQ] Exchange indisponivel; usando a fila direta.");
                if (!canal.isOpen()) {
                    canal = conexao.createChannel();
                    canal.queueDeclare(QUEUE_NAME, true, false, false, null);
                }
            }

            final Channel canalConsumo = canal;
            canalConsumo.basicQos(1);

            DeliverCallback callback = (consumerTag, delivery) -> {
                long tag = delivery.getEnvelope().getDeliveryTag();
                String mensagem = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    if (mensagem.isBlank() || delivery.getBody().length > 60000) {
                        throw new IllegalArgumentException("Mensagem vazia ou muito grande.");
                    }

                    // O desafio e uma mensagem de controle, nao um registro de auditoria.
                    if (responderDesafio(mensagem)) {
                        canalConsumo.basicAck(tag, false);
                        return;
                    }

                    salvarLogNoBanco(mensagem);
                } catch (IllegalArgumentException e) {
                    logger.warn("[RABBITMQ] Mensagem invalida rejeitada.");
                    canalConsumo.basicReject(tag, false);
                    return;
                } catch (RuntimeException e) {
                    logger.error("[RABBITMQ] Falha na persistencia da auditoria: {}",
                            e.getClass().getSimpleName());
                    // Reenvia a mensagem a fila para nova tentativa de persistencia.
                    // Uma politica de retentativas/DLQ devera ser definida no RF07.
                    canalConsumo.basicNack(tag, false, true);
                    return;
                }

                // Confirma a entrega somente apos a persistencia pelo repositorio.
                canalConsumo.basicAck(tag, false);
            };

            canalConsumo.basicConsume(QUEUE_NAME, false, callback, consumerTag -> {
                logger.warn("[RABBITMQ] Consumidor cancelado pelo broker.");
            });

            logger.info("[RABBITMQ] Aguardando mensagens na fila {}.", QUEUE_NAME);
        } catch (Exception e) {
            fecharConexao();
            throw new IOException("Falha ao iniciar consumidor RabbitMQ.", e);
        }
    }

    private static boolean responderDesafio(String mensagem) {
        if (CHALLENGE_TOKEN.isBlank() || REPLY_QUEUE.isBlank()) {
            return false;
        }

        String[] partes = mensagem.split(",", -1);
        if (partes.length == 2 && REPLY_QUEUE.equals(partes[0].trim())
                && CHALLENGE_TOKEN.equals(partes[1].trim())) {
            Enviar.enviarMensagem("true", REPLY_QUEUE);
            return true;
        }
        return false;
    }

    public static void salvarLogNoBanco(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Mensagem vazia.");
        }

        LogOperacaoRepository repository = logRepositoryEstatico;
        if (repository == null) {
            throw new IllegalStateException("Repositorio de auditoria indisponivel.");
        }

        LogOperacao log = new LogOperacao();
        log.setDataHora(LocalDateTime.now());
        String raw = message.trim();

        if (raw.startsWith("{")) {
            JsonNode json;
            try {
                json = objectMapper.readTree(raw);
                if (json == null || !json.isObject()) {
                    throw new IllegalArgumentException("Objeto JSON esperado.");
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("JSON invalido.", e);
            }

            log.setEntidade(limitar(getFirstText(json, "entidade", "modulo", "recurso", "origem"),
                    "CARRO", 40));
            log.setTipoOperacao(limitar(getFirstText(json, "tipoOperacao", "tipo", "operacao", "acao", "tipo_operacao"),
                    "OPERACAO", 50));
            log.setReferenciaId(getFirstLong(json, "referenciaId", "carroId", "id", "carId", "idCarro", "referencia_id"));
            log.setUsuarioId(getFirstLong(json, "usuarioId", "idUsuario", "userId", "usuario_id"));
            log.setUsuarioNome(limitar(getFirstText(json, "usuarioNome", "nomeUsuario", "usuario", "user", "usuario_nome"),
                    "Dupla RabbitMQ", 120));

            String detalhes = getFirstText(json, "detalhes", "mensagem", "descricao", "msg", "obs");
            log.setDetalhes(detalhes != null ? detalhes : raw);
        } else {
            // Mantem a compatibilidade com mensagens de texto da aula.
            log.setEntidade("CARRO");
            log.setTipoOperacao("OPERACAO_TEXTO");
            log.setUsuarioNome("Dupla RabbitMQ");
            log.setDetalhes(raw);
        }

        // A entidade LogOperacao mapeia tipoOperacao para a coluna acao.
        // Nao cria tabelas, nao altera colunas e nao tenta senhas do banco.
        LogOperacao salvo = repository.save(log);
        logger.info("[AUDITORIA] Registro recebido e salvo. ID: {}", salvo.getId());
    }

    private static String getFirstText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull() && value.isValueNode()) {
                String text = value.asText().trim();
                if (!text.isEmpty()) return text;
            }
        }
        return null;
    }

    private static Long getFirstLong(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull()) {
                try {
                    return Long.valueOf(value.asText().trim());
                } catch (NumberFormatException ignored) {
                    // Tenta o proximo nome de campo compativel.
                }
            }
        }
        return null;
    }

    private static String limitar(String valor, String padrao, int maximo) {
        String texto = valor == null || valor.isBlank() ? padrao : valor;
        int tamanho = texto.codePointCount(0, texto.length());
        return tamanho <= maximo ? texto : texto.substring(0, texto.offsetByCodePoints(0, maximo));
    }

    // Mantido apenas para compatibilidade de chamadas antigas; nao calcula auditorias novas.
    @Deprecated
    public static double calcularTaxaPorCargo(String cargo) {
        if (cargo == null) return 0.0;
        return switch (cargo.trim().toUpperCase(Locale.ROOT)) {
            case "VENDEDOR" -> 0.03;
            case "GERENTE" -> 0.02;
            case "SUPERUSUARIO" -> 0.01;
            default -> 0.0;
        };
    }

    @PreDestroy
    public void encerrarConsumidor() {
        fecharConexao();
        logRepositoryEstatico = null;
    }

    private static synchronized void fecharConexao() {
        if (canal != null) {
            try {
                canal.close();
            } catch (Exception ignored) {
            }
            canal = null;
        }
        if (conexao != null) {
            try {
                conexao.close();
            } catch (Exception ignored) {
            }
            conexao = null;
        }
    }
}
