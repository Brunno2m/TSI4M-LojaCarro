package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

/**
 * Módulo de Envio de Mensagens - CRUD CARRO:
 * Responsável por publicar eventos de veículos (Cadastro, Atualização, Exclusão)
 * no broker RabbitMQ no IP 15.228.215.36.
 *
 * Pode ser executado de forma independente pelo método main().
 */
@Component
public class EnviarCarro extends Enviar {

    public static final String QUEUE_CARRO = DEFAULT_QUEUE;

    public static void main(String[] args) {
        System.out.println(">>> Executando módulo isolado: EnviarCarro no IP " + HOST + "...");

        // Exemplo 1: Cadastro de Carro
        enviarCadastro(101L, "Honda Civic Touring", 2024, 165000.0, "Carlos Gerente");

        // Exemplo 2: Atualização de Carro
        enviarAtualizacao(101L, "Honda Civic Touring", 2024, 159900.0, "Carlos Gerente");

        // Exemplo 3: Exclusão de Carro
        enviarExclusao(101L, "Honda Civic Touring", "Carlos Gerente");
    }

    public static void enviarCadastro(Long id, String modelo, int ano, double preco, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CADASTRO",
                id,
                modelo,
                ano,
                preco,
                1L,
                usuario != null ? usuario : "Sistema",
                "ESTOQUE",
                String.format("Carro cadastrado: %s (Ano %d) por R$ %.2f", modelo, ano, preco)
        );
        dto.setEntidade("CARRO");
        enviarMensagemDireta(dto);
    }

    public static void enviarAtualizacao(Long id, String modelo, int ano, double preco, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "ATUALIZACAO",
                id,
                modelo,
                ano,
                preco,
                1L,
                usuario != null ? usuario : "Sistema",
                "ESTOQUE",
                String.format("Carro atualizado: %s (Ano %d) - Novo preço: R$ %.2f", modelo, ano, preco)
        );
        dto.setEntidade("CARRO");
        enviarMensagemDireta(dto);
    }

    public static void enviarExclusao(Long id, String modelo, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "EXCLUSAO",
                id,
                modelo,
                null,
                null,
                1L,
                usuario != null ? usuario : "Sistema",
                "ESTOQUE",
                String.format("Carro removido do estoque: %s (ID: %d)", modelo, id)
        );
        dto.setEntidade("CARRO");
        enviarMensagemDireta(dto);
    }

    private static void enviarMensagemDireta(LogOperacaoDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            enviarMensagem(json, QUEUE_CARRO);
        } catch (Exception e) {
            System.err.println("Erro ao serializar log do Carro: " + e.getMessage());
        }
    }
}
