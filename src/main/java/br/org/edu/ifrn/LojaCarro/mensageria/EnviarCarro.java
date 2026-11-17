
package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

@Component
public class EnviarCarro extends Enviar {

    public static final String QUEUE_CARRO = DEFAULT_QUEUE;

    // Verificacao isolada da configuracao
    public static void main(String[] args) {

        System.out.println("=== MODULO DE AUDITORIA DE VEICULOS ===");
        System.out.println("RabbitMQ habilitado: " + ENABLED);
        System.out.println("Servidor: " + HOST);
        System.out.println("Fila: " + QUEUE_CARRO);

        if (!ENABLED) {
            System.out.println("Mensageria desativada.");
        }
    }

    // Registro de cadastro de veiculo
    public static void enviarCadastro(
            Long id,
            String modelo,
            int ano,
            double preco,
            String usuario
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CADASTRO",
                id,
                modelo,
                ano,
                preco,
                null,
                usuario != null ? usuario : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Carro cadastrado: %s (Ano %d) por R$ %.2f",
                        modelo,
                        ano,
                        preco
                )
        );

        dto.setEntidade("CARRO");

        enviarMensagemDireta(dto);
    }

    // Registro de atualizacao de veiculo
    public static void enviarAtualizacao(
            Long id,
            String modelo,
            int ano,
            double preco,
            String usuario
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "ATUALIZACAO",
                id,
                modelo,
                ano,
                preco,
                null,
                usuario != null ? usuario : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Carro atualizado: %s (Ano %d) - Novo preco: R$ %.2f",
                        modelo,
                        ano,
                        preco
                )
        );

        dto.setEntidade("CARRO");

        enviarMensagemDireta(dto);
    }

    // Registro de exclusao logica de veiculo
    public static void enviarExclusao(
            Long id,
            String modelo,
            String usuario
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "EXCLUSAO",
                id,
                modelo,
                null,
                null,
                null,
                usuario != null ? usuario : "Sistema",
                "NAO_INFORMADO",
                String.format(
                        "Carro desativado no estoque: %s (ID: %d)",
                        modelo,
                        id
                )
        );

        dto.setEntidade("CARRO");

        enviarMensagemDireta(dto);
    }

    // Conversao e envio do DTO
    private static void enviarMensagemDireta(
            LogOperacaoDTO dto
    ) {

        if (!ENABLED) {

            System.out.println(
                    "[AUDITORIA CARRO] RabbitMQ desativado."
            );

            return;
        }

        if (dto == null) {
            return;
        }

        try {

            String json = objectMapper.writeValueAsString(dto);

            enviarMensagem(json, QUEUE_CARRO);

        } catch (Exception e) {

            System.err.println(
                    "[AUDITORIA CARRO] Erro ao serializar evento: "
                            + e.getMessage()
            );
        }
    }
}
