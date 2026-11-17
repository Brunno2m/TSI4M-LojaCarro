
package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

@Component
public class EnviarTesteDrive extends Enviar {

    public static final String QUEUE_TESTE_DRIVE = DEFAULT_QUEUE;

    // Verificacao isolada da configuracao
    public static void main(String[] args) {

        System.out.println("=== MODULO DE AUDITORIA DE TEST DRIVE ===");
        System.out.println("RabbitMQ habilitado: " + ENABLED);
        System.out.println("Servidor: " + HOST);
        System.out.println("Fila: " + QUEUE_TESTE_DRIVE);

        if (!ENABLED) {
            System.out.println("Mensageria desativada.");
        }
    }

    // Registro de agendamento de test drive
    public static void enviarAgendamento(
            Long id,
            String cliente,
            String modelo,
            String dataHora,
            String responsavel
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "CADASTRO",
                id,
                0.0,
                null,
                responsavel != null ? responsavel : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Test Drive agendado: Cliente %s com %s para %s",
                        cliente,
                        modelo,
                        dataHora
                )
        );

        dto.setCarroModelo(modelo);

        enviarMensagemDireta(dto);
    }

    // Registro de alteracao do status
    public static void enviarStatus(
            Long id,
            String cliente,
            String modelo,
            String status,
            String responsavel
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "ATUALIZACAO",
                id,
                0.0,
                null,
                responsavel != null ? responsavel : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Test Drive atualizado: ID %d (%s com %s) - Status: %s",
                        id,
                        cliente,
                        modelo,
                        status
                )
        );

        dto.setCarroModelo(modelo);

        enviarMensagemDireta(dto);
    }

    // Registro de cancelamento de agendamento
    public static void enviarCancelamento(
            Long id,
            String cliente,
            String modelo,
            String responsavel
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "CANCELAMENTO",
                id,
                0.0,
                null,
                responsavel != null ? responsavel : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Test Drive cancelado: ID %d (Cliente %s com %s)",
                        id,
                        cliente,
                        modelo
                )
        );

        dto.setCarroModelo(modelo);

        enviarMensagemDireta(dto);
    }

    // Conversao e envio do DTO ao RabbitMQ
    private static void enviarMensagemDireta(
            LogOperacaoDTO dto
    ) {

        if (!ENABLED) {

            System.out.println(
                    "[AUDITORIA TEST DRIVE] RabbitMQ desativado."
            );

            return;
        }

        if (dto == null) {
            return;
        }

        try {

            String json = objectMapper.writeValueAsString(dto);

            enviarMensagem(json, QUEUE_TESTE_DRIVE);

        } catch (Exception e) {

            System.err.println(
                    "[AUDITORIA TEST DRIVE] Erro ao serializar evento: "
                            + e.getMessage()
            );
        }
    }
}
