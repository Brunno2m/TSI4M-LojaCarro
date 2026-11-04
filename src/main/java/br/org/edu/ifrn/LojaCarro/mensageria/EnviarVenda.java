package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

/**
 * Módulo de Envio de Mensagens - CRUD VENDAS:
 * Responsável por publicar eventos de vendas no broker RabbitMQ (15.228.215.36).
 *
 * Pode ser executado de forma independente pelo método main().
 */
@Component
public class EnviarVenda extends Enviar {

    public static final String QUEUE_VENDA = DEFAULT_QUEUE;

    public static void main(String[] args) {
        System.out.println(">>> Executando módulo isolado: EnviarVenda no IP " + HOST + "...");

        // Exemplo 1: Conclusão de Venda por VENDEDOR (Comissão: 3%)
        enviarCadastro(201L, "Toyota Corolla Cross", "Marcos Vinicius", 175000.0, "PIX", "Ana Vendedora", "VENDEDOR");

        // Exemplo 2: Conclusão de Venda por GERENTE (Comissão: 2%)
        enviarCadastro(202L, "Honda Civic Touring", "Juliana Costa", 160000.0, "FINANCIAMENTO", "Carlos Gerente", "GERENTE");

        // Exemplo 3: Conclusão de Venda por SUPERUSUARIO (Comissão: 1%)
        enviarCadastro(203L, "Jeep Compass Limited", "Roberto Santos", 180000.0, "A_VISTA", "Administrador Geral", "SUPERUSUARIO");

        // Exemplo 4: Atualização de Venda
        enviarAtualizacao(201L, "Toyota Corolla Cross", 175000.0, "CONCLUIDA", "Ana Vendedora");

        // Exemplo 5: Cancelamento de Venda
        enviarExclusao(201L, "Toyota Corolla Cross", 175000.0, "Ana Vendedora");
    }

    public static double obterPercentualComissaoPorCargo(String cargo) {
        if (cargo == null) return 0.0;
        switch (cargo.trim().toUpperCase()) {
            case "VENDEDOR": return 0.03;
            case "GERENTE": return 0.02;
            case "SUPERUSUARIO": return 0.01;
            default: return 0.0;
        }
    }

    public static void enviarCadastro(Long id, String modelo, String cliente, double valor, String formaPagamento, String vendedor) {
        enviarCadastro(id, modelo, cliente, valor, formaPagamento, vendedor, "VENDEDOR");
    }

    public static void enviarCadastro(Long id, String modelo, String cliente, double valor, String formaPagamento, String vendedor, String cargo) {
        String cargoEfetivo = cargo != null ? cargo : "VENDEDOR";
        double taxa = obterPercentualComissaoPorCargo(cargoEfetivo);
        double valorComissao = Math.round((valor * taxa) * 100.0) / 100.0;
        double pctDisplay = Math.round((taxa * 100.0) * 10.0) / 10.0;

        String detalhes = String.format("Venda concluída: %s para %s por R$ %.2f (Forma: %s) | Vendedor: %s (%s) | Comissão (%.1f%%): R$ %.2f",
                modelo, cliente, valor, formaPagamento, vendedor, cargoEfetivo, pctDisplay, valorComissao);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "VENDAS",
                "CADASTRO",
                id,
                valor,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                cargoEfetivo,
                detalhes
        );
        dto.setCarroModelo(modelo);
        dto.setPercentualComissao(pctDisplay);
        dto.setValorComissao(valorComissao);
        enviarMensagemDireta(dto);
    }

    public static void enviarAtualizacao(Long id, String modelo, double valor, String status, String vendedor) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "VENDAS",
                "ATUALIZACAO",
                id,
                valor,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                "VENDEDOR",
                String.format("Venda atualizada: ID %d (%s) - Status: %s", id, modelo, status)
        );
        dto.setCarroModelo(modelo);
        enviarMensagemDireta(dto);
    }

    public static void enviarExclusao(Long id, String modelo, double valor, String vendedor) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "VENDAS",
                "EXCLUSAO",
                id,
                valor,
                1L,
                vendedor != null ? vendedor : "Vendedor",
                "VENDEDOR",
                String.format("Venda cancelada/estornada: ID %d (%s, R$ %.2f)", id, modelo, valor)
        );
        dto.setCarroModelo(modelo);
        enviarMensagemDireta(dto);
    }

    private static void enviarMensagemDireta(LogOperacaoDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            enviarMensagem(json, QUEUE_VENDA);
        } catch (Exception e) {
            System.err.println("Erro ao serializar log de Venda: " + e.getMessage());
        }
    }
}
