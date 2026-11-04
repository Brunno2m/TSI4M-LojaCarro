package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

/**
 * Módulo de Envio de Mensagens - CRUD TESTE DRIVE:
 * Responsável por publicar eventos de agendamento e realização de test drives
 * no broker RabbitMQ (15.228.215.36).
 *
 * Pode ser executado de forma independente pelo método main().
 */
@Component
public class EnviarTesteDrive extends Enviar {

    public static final String QUEUE_TESTE_DRIVE = DEFAULT_QUEUE;

    public static void main(String[] args) {
        System.out.println(">>> Executando módulo isolado: EnviarTesteDrive no IP " + HOST + "...");

        // Exemplo 1: Agendamento de Test Drive
        enviarAgendamento(401L, "Fernanda Lima", "Jeep Compass (2023)", "2026-11-06 10:30:00", "Ana Vendedora");

        // Exemplo 2: Conclusão do Test Drive
        enviarStatus(401L, "Fernanda Lima", "Jeep Compass (2023)", "REALIZADO", "Ana Vendedora");

        // Exemplo 3: Cancelamento
        enviarCancelamento(401L, "Fernanda Lima", "Jeep Compass (2023)", "Ana Vendedora");
    }

    public static void enviarAgendamento(Long id, String cliente, String modelo, String dataHora, String vendedor) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "CADASTRO",
                id,
                0.0,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                "VENDEDOR",
                String.format("Test Drive agendado: Cliente %s com %s para %s", cliente, modelo, dataHora)
        );
        dto.setCarroModelo(modelo);
        enviarMensagemDireta(dto);
    }

    public static void enviarStatus(Long id, String cliente, String modelo, String status, String vendedor) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "ATUALIZACAO",
                id,
                0.0,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                "VENDEDOR",
                String.format("Test Drive atualizado: ID %d (%s com %s) - Status: %s", id, cliente, modelo, status)
        );
        dto.setCarroModelo(modelo);
        enviarMensagemDireta(dto);
    }

    public static void enviarCancelamento(Long id, String cliente, String modelo, String vendedor) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "EXCLUSAO",
                id,
                0.0,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                "VENDEDOR",
                String.format("Test Drive cancelado: ID %d (Cliente %s com %s)", id, cliente, modelo)
        );
        dto.setCarroModelo(modelo);
        enviarMensagemDireta(dto);
    }

    private static void enviarMensagemDireta(LogOperacaoDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            enviarMensagem(json, QUEUE_TESTE_DRIVE);
        } catch (Exception e) {
            System.err.println("Erro ao serializar log de Test Drive: " + e.getMessage());
        }
    }
}
