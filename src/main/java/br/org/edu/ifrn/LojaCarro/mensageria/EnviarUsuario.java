
package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

@Component
public class EnviarUsuario extends Enviar {

    public static final String QUEUE_USUARIO = DEFAULT_QUEUE;

    // Verificacao isolada da configuracao
    public static void main(String[] args) {

        System.out.println("=== MODULO DE AUDITORIA DE USUARIOS ===");
        System.out.println("RabbitMQ habilitado: " + ENABLED);
        System.out.println("Servidor: " + HOST);
        System.out.println("Fila: " + QUEUE_USUARIO);

        if (!ENABLED) {
            System.out.println("Mensageria desativada.");
        }
    }

    // Registro de cadastro de usuario
    public static void enviarCadastro(
            Long id,
            String nome,
            String cargo,
            String login,
            String responsavel
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "CADASTRO",
                id,
                0.0,
                null,
                responsavel != null ? responsavel : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Usuario cadastrado: %s (Perfil: %s)",
                        nome,
                        cargo
                )
        );

        enviarMensagemDireta(dto);
    }

    // Registro de atualizacao de usuario
    public static void enviarAtualizacao(
            Long id,
            String nome,
            String cargo,
            String login,
            String responsavel
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "ATUALIZACAO",
                id,
                0.0,
                null,
                responsavel != null ? responsavel : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Usuario atualizado: %s (Perfil: %s)",
                        nome,
                        cargo
                )
        );

        enviarMensagemDireta(dto);
    }

    // Registro de exclusao de usuario
    public static void enviarExclusao(
            Long id,
            String nome,
            String cargo,
            String responsavel
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "EXCLUSAO",
                id,
                0.0,
                null,
                responsavel != null ? responsavel : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Usuario excluido: %s (Perfil: %s, ID: %d)",
                        nome,
                        cargo,
                        id
                )
        );

        enviarMensagemDireta(dto);
    }

    // Conversao e envio do DTO
    private static void enviarMensagemDireta(
            LogOperacaoDTO dto
    ) {

        if (!ENABLED) {

            System.out.println(
                    "[AUDITORIA USUARIO] RabbitMQ desativado."
            );

            return;
        }

        if (dto == null) {
            return;
        }

        try {

            String json = objectMapper.writeValueAsString(dto);

            enviarMensagem(json, QUEUE_USUARIO);

        } catch (Exception e) {

            System.err.println(
                    "[AUDITORIA USUARIO] Erro ao serializar evento: "
                            + e.getMessage()
            );
        }
    }
}
