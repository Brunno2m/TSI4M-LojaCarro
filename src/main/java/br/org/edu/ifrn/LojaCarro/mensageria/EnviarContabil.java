package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

/**
 * Módulo de Envio de Mensagens - CRUD CONTÁBIL:
 * Responsável por publicar eventos financeiros (Entradas, Saídas, Ajustes)
 * no broker RabbitMQ (15.228.215.36).
 *
 * Pode ser executado de forma independente pelo método main().
 */
@Component
public class EnviarContabil extends Enviar {

    public static final String QUEUE_CONTABIL = DEFAULT_QUEUE;

    public static void main(String[] args) {
        System.out.println(">>> Executando módulo isolado: EnviarContabil no IP " + HOST + "...");

        // Exemplo 1: Lançamento de Receita (ENTRADA)
        enviarEntrada(301L, "Recebimento Parcial Financiamento Santander", 42000.0, "VENDA", "Roberto Contador");

        // Exemplo 2: Lançamento de Despesa (SAIDA)
        enviarSaida(302L, "Pagamento de Comissão Vendedor - Ana", 2500.0, "COMISSAO", "Roberto Contador");

        // Exemplo 3: Exclusão de Lançamento
        enviarExclusao(302L, "Pagamento de Comissão Vendedor - Ana", 2500.0, "Roberto Contador");
    }

    public static void enviarEntrada(Long id, String descricao, double valor, String categoria, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CONTABIL",
                "CADASTRO",
                id,
                valor,
                1L,
                usuario != null ? usuario : "Contador",
                "CONTABIL",
                String.format("Lançamento Contábil ENTRADA: %s | R$ %.2f [%s]", descricao, valor, categoria)
        );
        enviarMensagemDireta(dto);
    }

    public static void enviarSaida(Long id, String descricao, double valor, String categoria, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CONTABIL",
                "CADASTRO",
                id,
                valor,
                1L,
                usuario != null ? usuario : "Contador",
                "CONTABIL",
                String.format("Lançamento Contábil SAIDA: %s | R$ %.2f [%s]", descricao, valor, categoria)
        );
        enviarMensagemDireta(dto);
    }

    public static void enviarExclusao(Long id, String descricao, double valor, String usuario) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CONTABIL",
                "EXCLUSAO",
                id,
                valor,
                1L,
                usuario != null ? usuario : "Contador",
                "CONTABIL",
                String.format("Lançamento Contábil Estornado/Excluído: ID %d (%s, R$ %.2f)", id, descricao, valor)
        );
        enviarMensagemDireta(dto);
    }

    private static void enviarMensagemDireta(LogOperacaoDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            enviarMensagem(json, QUEUE_CONTABIL);
        } catch (Exception e) {
            System.err.println("Erro ao serializar log Contábil: " + e.getMessage());
        }
    }
}
