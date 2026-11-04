package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Módulo de Log - Consumidor RabbitMQ:
 * Responsável por receber os dados da mensagem da dupla e salvar na tabela de logs.
 * Configurado para o IP 15.228.215.36.
 */
@Component
public class Receber {

    public static final String HOST = "15.228.215.36";
    public static final int PORT = 5672;
    public static final String USER = "aluno";
    public static final String PASS = "12345";
    public static final String QUEUE_NAME = "guilherme_brunno";

    // Repositório Spring injetado quando a aplicação estiver rodando
    private static LogOperacaoRepository logRepositoryEstatico;

    @Autowired
    public void setLogOperacaoRepository(LogOperacaoRepository repository) {
        Receber.logRepositoryEstatico = repository;
    }

    static Enviar l = new Enviar();
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public static void main(String[] args) {
        try {
            lerMensagem();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void lerMensagem() throws IOException {
        ConnectionFactory factory;
        Connection connection;
        Channel channel;

        String QUEUE_NAME1 = QUEUE_NAME;
        String EXCHANGE_NAME1 = QUEUE_NAME;
        try {
            factory = new ConnectionFactory();
            factory.setHost(HOST);
            factory.setPort(PORT);
            factory.setUsername(USER);
            factory.setPassword(PASS);

            connection = factory.newConnection();
            channel = connection.createChannel();

            // Declara a fila 'guilherme_brunno'
            channel.queueDeclare(QUEUE_NAME1, true, false, false, null);

            // Tenta vincular à exchange caso o parceiro envie via exchange
            try {
                channel.exchangeDeclare(EXCHANGE_NAME1, "direct", true, false, null);
                channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME1, "");
                channel.queueBind(QUEUE_NAME1, EXCHANGE_NAME1, QUEUE_NAME1);
            } catch (Exception exEx) {
                // Se a exchange não for usada ou já existir com outros parâmetros, garante canal aberto
                if (!channel.isOpen()) {
                    channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME1, true, false, false, null);
                }
            }

            System.out.println(" [*] Aguardando mensagens no RabbitMQ (" + HOST + ":" + PORT + " - fila: " + QUEUE_NAME1 + ")...");

            DeliverCallback deliverCallbackQuestion = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("MENSAGEM RECEBIDA = '" + message + "'");

                // Lógica de resposta solicitada pelo professor
                String partes[] = message.split(",");
                if (partes.length > 1 && partes[1].trim().equals("12345")) {
                    Enviar.enviarMensagem("true", partes[0].trim());
                }

                // Salva os dados recebidos da mensagem na tabela de logs do banco de dados
                salvarLogNoBanco(message);
            };

            channel.basicConsume(QUEUE_NAME1, true, deliverCallbackQuestion, consumerTag -> {
            });

        } catch (Exception e) {
            System.out.println("Erro na conexão com RabbitMQ (" + HOST + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Processa o conteúdo recebido (JSON ou texto) e salva na tabela de logs (log_operacao).
     *
     * @param message Conteúdo da mensagem enviada pela dupla ou módulos do sistema
     */
    public static void salvarLogNoBanco(String message) {
        try {
            LogOperacao logOperacao = new LogOperacao();
            logOperacao.setDataHora(LocalDateTime.now());

            if (message != null && message.trim().startsWith("{")) {
                // Mensagem em formato JSON
                JsonNode json = objectMapper.readTree(message);

                String entidade = getFirstText(json, "entidade", "modulo", "recurso", "origem");
                logOperacao.setEntidade(entidade != null ? entidade : "CARRO");

                String tipo = getFirstText(json, "tipoOperacao", "tipo", "operacao", "acao", "tipo_operacao");
                logOperacao.setTipoOperacao(tipo != null ? tipo : "OPERACAO");

                Long refId = getFirstLong(json, "referenciaId", "carroId", "id", "carId", "idCarro", "referencia_id");
                logOperacao.setReferenciaId(refId);
                logOperacao.setCarroId(getFirstLong(json, "carroId", "id", "carId", "idCarro"));
                logOperacao.setCarroModelo(getFirstText(json, "carroModelo", "modelo", "carro", "nome", "carro_modelo"));
                logOperacao.setCarroAno(getFirstInt(json, "carroAno", "ano", "year", "carro_ano"));

                Double valor = getFirstDouble(json, "valor", "carroPreco", "preco", "price", "carro_preco", "valorVenda");
                logOperacao.setValor(valor);
                logOperacao.setCarroPreco(valor);

                logOperacao.setUsuarioId(getFirstLong(json, "usuarioId", "idUsuario", "userId", "usuario_id"));
                String usuarioNome = getFirstText(json, "usuarioNome", "nomeUsuario", "usuario", "user", "nome", "usuario_nome");
                logOperacao.setUsuarioNome(usuarioNome != null ? usuarioNome : "Dupla RabbitMQ");

                String usuarioCargo = getFirstText(json, "usuarioCargo", "cargo", "role", "funcao", "usuario_cargo");
                logOperacao.setUsuarioCargo(usuarioCargo != null ? usuarioCargo : "MENSAGERIA");

                String detalhes = getFirstText(json, "detalhes", "mensagem", "descricao", "msg", "obs");
                logOperacao.setDetalhes(detalhes != null ? detalhes : message);

                Double percentualComissao = getFirstDouble(json, "percentualComissao", "percentual_comissao", "comissaoPercentual", "taxaComissao");
                Double valorComissao = getFirstDouble(json, "valorComissao", "valor_comissao", "comissao", "comissaoValor");

                if ("VENDAS".equalsIgnoreCase(entidade) && "CADASTRO".equalsIgnoreCase(tipo)) {
                    if (percentualComissao == null || percentualComissao == 0.0) {
                        double taxa = calcularTaxaPorCargo(usuarioCargo);
                        percentualComissao = Math.round(taxa * 100.0 * 10.0) / 10.0;
                    }
                    if ((valorComissao == null || valorComissao == 0.0) && valor != null && valor > 0 && percentualComissao > 0) {
                        valorComissao = Math.round((valor * (percentualComissao / 100.0)) * 100.0) / 100.0;
                    }
                }

                logOperacao.setPercentualComissao(percentualComissao);
                logOperacao.setValorComissao(valorComissao);
            } else {
                // Mensagem em formato texto simples ou separada por vírgula
                String raw = message != null ? message : "";
                String[] partes = raw.split(",");
                logOperacao.setEntidade("CARRO");
                logOperacao.setTipoOperacao("OPERACAO_TEXTO");
                if (partes.length >= 2) {
                    logOperacao.setCarroModelo(partes[0].trim());
                    try {
                        double p = Double.parseDouble(partes[1].trim());
                        logOperacao.setCarroPreco(p);
                        logOperacao.setValor(p);
                    } catch (NumberFormatException ignored) {}
                } else if (!raw.trim().isEmpty()) {
                    logOperacao.setCarroModelo(raw.trim());
                }
                logOperacao.setUsuarioNome("Dupla RabbitMQ");
                logOperacao.setUsuarioCargo("MENSAGERIA");
                logOperacao.setDetalhes(raw);
            }

            if (logOperacao.getTipoOperacao() == null || logOperacao.getTipoOperacao().trim().isEmpty()) {
                logOperacao.setTipoOperacao("OPERACAO");
            }

            // 1. Se o Spring estiver rodando, salva via LogOperacaoRepository (JPA)
            if (logRepositoryEstatico != null) {
                LogOperacao salvo = logRepositoryEstatico.save(logOperacao);
                System.out.println(">>> SUCESSO: Dados da mensagem salvos na tabela de logs via JPA! ID: " + salvo.getId());
                return;
            }

            // 2. Fallback via JDBC caso seja executado via main() isolado fora do Spring Boot
            salvarViaJdbcFallback(logOperacao);

        } catch (Exception e) {
            System.out.println("Erro ao salvar mensagem na tabela de logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static double calcularTaxaPorCargo(String cargo) {
        if (cargo == null) return 0.0;
        switch (cargo.trim().toUpperCase()) {
            case "VENDEDOR":
                return 0.03; // 3%
            case "GERENTE":
                return 0.02; // 2%
            case "SUPERUSUARIO":
                return 0.01; // 1%
            default:
                return 0.0;
        }
    }

    private static String getFirstText(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                String val = node.get(key).asText();
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
        }
        return null;
    }

    private static Long getFirstLong(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                try {
                    return node.get(key).asLong();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static Integer getFirstInt(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                try {
                    return node.get(key).asInt();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static Double getFirstDouble(JsonNode node, String... keys) {
        for (String key : keys) {
            if (node.has(key) && !node.get(key).isNull()) {
                try {
                    return node.get(key).asDouble();
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static void salvarViaJdbcFallback(LogOperacao log) {
        String url = "jdbc:mysql://localhost:3306/carros?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String[] possiblePasswords = {"", "root", "admin", "12345", "123456"};

        java.sql.Connection conn = null;
        for (String pass : possiblePasswords) {
            try {
                conn = DriverManager.getConnection(url, user, pass);
                if (conn != null) break;
            } catch (Exception ignored) {
            }
        }

        if (conn == null) {
            System.err.println("Erro ao persistir log via JDBC: não foi possível conectar ao banco MySQL (port 3306).");
            return;
        }

        String createTableSql = "CREATE TABLE IF NOT EXISTS log_operacao (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "entidade VARCHAR(30), " +
                "tipo_operacao VARCHAR(30) NOT NULL, " +
                "referencia_id BIGINT, " +
                "carro_id BIGINT, " +
                "carro_modelo VARCHAR(100), " +
                "carro_ano INT, " +
                "carro_preco DOUBLE, " +
                "valor DOUBLE, " +
                "percentual_comissao DOUBLE, " +
                "valor_comissao DOUBLE, " +
                "usuario_id BIGINT, " +
                "usuario_nome VARCHAR(100), " +
                "usuario_cargo VARCHAR(50), " +
                "data_hora DATETIME NOT NULL, " +
                "detalhes TEXT)";

        try (java.sql.Connection c = conn) {
            try (PreparedStatement stmtCreate = c.prepareStatement(createTableSql)) {
                stmtCreate.execute();
            }

            // Tenta adicionar colunas caso tabela já existisse em versão anterior
            try (PreparedStatement stmtAlter = c.prepareStatement("ALTER TABLE log_operacao ADD COLUMN IF NOT EXISTS entidade VARCHAR(30)")) {
                stmtAlter.execute();
            } catch (Exception ignored) {}
            try (PreparedStatement stmtAlter = c.prepareStatement("ALTER TABLE log_operacao ADD COLUMN IF NOT EXISTS referencia_id BIGINT")) {
                stmtAlter.execute();
            } catch (Exception ignored) {}
            try (PreparedStatement stmtAlter = c.prepareStatement("ALTER TABLE log_operacao ADD COLUMN IF NOT EXISTS valor DOUBLE")) {
                stmtAlter.execute();
            } catch (Exception ignored) {}
            try (PreparedStatement stmtAlter = c.prepareStatement("ALTER TABLE log_operacao ADD COLUMN IF NOT EXISTS percentual_comissao DOUBLE")) {
                stmtAlter.execute();
            } catch (Exception ignored) {}
            try (PreparedStatement stmtAlter = c.prepareStatement("ALTER TABLE log_operacao ADD COLUMN IF NOT EXISTS valor_comissao DOUBLE")) {
                stmtAlter.execute();
            } catch (Exception ignored) {}

            String insertSql = "INSERT INTO log_operacao (entidade, tipo_operacao, referencia_id, carro_id, carro_modelo, carro_ano, carro_preco, valor, " +
                    "percentual_comissao, valor_comissao, usuario_id, usuario_nome, usuario_cargo, data_hora, detalhes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = c.prepareStatement(insertSql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, log.getEntidade());
                stmt.setString(2, log.getTipoOperacao() != null ? log.getTipoOperacao() : "OPERACAO");
                if (log.getReferenciaId() != null) stmt.setLong(3, log.getReferenciaId()); else stmt.setNull(3, java.sql.Types.BIGINT);
                if (log.getCarroId() != null) stmt.setLong(4, log.getCarroId()); else stmt.setNull(4, java.sql.Types.BIGINT);
                stmt.setString(5, log.getCarroModelo());
                if (log.getCarroAno() != null) stmt.setInt(6, log.getCarroAno()); else stmt.setNull(6, java.sql.Types.INTEGER);
                if (log.getCarroPreco() != null) stmt.setDouble(7, log.getCarroPreco()); else stmt.setNull(7, java.sql.Types.DOUBLE);
                if (log.getValor() != null) stmt.setDouble(8, log.getValor()); else stmt.setNull(8, java.sql.Types.DOUBLE);
                if (log.getPercentualComissao() != null) stmt.setDouble(9, log.getPercentualComissao()); else stmt.setNull(9, java.sql.Types.DOUBLE);
                if (log.getValorComissao() != null) stmt.setDouble(10, log.getValorComissao()); else stmt.setNull(10, java.sql.Types.DOUBLE);
                if (log.getUsuarioId() != null) stmt.setLong(11, log.getUsuarioId()); else stmt.setNull(11, java.sql.Types.BIGINT);
                stmt.setString(12, log.getUsuarioNome() != null ? log.getUsuarioNome() : "Dupla RabbitMQ");
                stmt.setString(13, log.getUsuarioCargo() != null ? log.getUsuarioCargo() : "MENSAGERIA");
                stmt.setTimestamp(14, Timestamp.valueOf(log.getDataHora() != null ? log.getDataHora() : LocalDateTime.now()));
                stmt.setString(15, log.getDetalhes());

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        log.setId(generatedId);
                        System.out.println(">>> SUCESSO: Dados da mensagem salvos na tabela de logs (log_operacao) via JDBC! ID: " + generatedId);
                    } else {
                        System.out.println(">>> SUCESSO: Dados da mensagem salvos na tabela de logs (log_operacao) via JDBC!");
                    }
                }
            } catch (Exception exInsert) {
                // Fallback para schema legado de 10 colunas
                String legacyInsert = "INSERT INTO log_operacao (tipo_operacao, carro_id, carro_modelo, carro_ano, carro_preco, " +
                        "usuario_id, usuario_nome, usuario_cargo, data_hora, detalhes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmtLegacy = c.prepareStatement(legacyInsert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    stmtLegacy.setString(1, log.getTipoOperacao() != null ? log.getTipoOperacao() : "OPERACAO");
                    if (log.getCarroId() != null) stmtLegacy.setLong(2, log.getCarroId()); else stmtLegacy.setNull(2, java.sql.Types.BIGINT);
                    stmtLegacy.setString(3, log.getCarroModelo());
                    if (log.getCarroAno() != null) stmtLegacy.setInt(4, log.getCarroAno()); else stmtLegacy.setNull(4, java.sql.Types.INTEGER);
                    if (log.getCarroPreco() != null) stmtLegacy.setDouble(5, log.getCarroPreco()); else stmtLegacy.setNull(5, java.sql.Types.DOUBLE);
                    if (log.getUsuarioId() != null) stmtLegacy.setLong(6, log.getUsuarioId()); else stmtLegacy.setNull(6, java.sql.Types.BIGINT);
                    stmtLegacy.setString(7, log.getUsuarioNome() != null ? log.getUsuarioNome() : "Dupla RabbitMQ");
                    stmtLegacy.setString(8, log.getUsuarioCargo() != null ? log.getUsuarioCargo() : "MENSAGERIA");
                    stmtLegacy.setTimestamp(9, Timestamp.valueOf(log.getDataHora() != null ? log.getDataHora() : LocalDateTime.now()));
                    stmtLegacy.setString(10, log.getDetalhes());

                    stmtLegacy.executeUpdate();
                    System.out.println(">>> SUCESSO: Dados da mensagem salvos via fallback legado JDBC!");
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao persistir log via JDBC: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
