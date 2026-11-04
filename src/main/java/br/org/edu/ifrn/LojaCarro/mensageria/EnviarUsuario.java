package br.org.edu.ifrn.LojaCarro.mensageria;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import org.springframework.stereotype.Component;

/**
 * Módulo de Envio de Mensagens - CRUD USUÁRIO:
 * Responsável por publicar eventos de gestão de equipe (Cadastro, Alteração de Cargo, Exclusão)
 * no broker RabbitMQ (15.228.215.36).
 *
 * Pode ser executado de forma independente pelo método main().
 */
@Component
public class EnviarUsuario extends Enviar {

    public static final String QUEUE_USUARIO = DEFAULT_QUEUE;

    public static void main(String[] args) {
        System.out.println(">>> Executando módulo isolado: EnviarUsuario no IP " + HOST + "...");

        // Exemplo 1: Cadastro de Usuário
        enviarCadastro(501L, "Gabriel Estagiário", "VENDEDOR", "gabriel", "Carlos Gerente");

        // Exemplo 2: Alteração de Usuário
        enviarAtualizacao(501L, "Gabriel Vendedor Pleno", "VENDEDOR", "gabriel", "Carlos Gerente");

        // Exemplo 3: Exclusão de Usuário
        enviarExclusao(501L, "Gabriel Vendedor Pleno", "VENDEDOR", "Carlos Gerente");
    }

    public static void enviarCadastro(Long id, String nome, String cargo, String login, String responsavel) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "CADASTRO",
                id,
                0.0,
                1L,
                responsavel != null ? responsavel : "Admin",
                "ADMINISTRACAO",
                String.format("Usuário cadastrado: %s (Cargo: %s, Login: %s) por %s", nome, cargo, login, responsavel)
        );
        enviarMensagemDireta(dto);
    }

    public static void enviarAtualizacao(Long id, String nome, String cargo, String login, String responsavel) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "ATUALIZACAO",
                id,
                0.0,
                1L,
                responsavel != null ? responsavel : "Admin",
                "ADMINISTRACAO",
                String.format("Usuário atualizado: %s (Cargo: %s, Login: %s) por %s", nome, cargo, login, responsavel)
        );
        enviarMensagemDireta(dto);
    }

    public static void enviarExclusao(Long id, String nome, String cargo, String responsavel) {
        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "EXCLUSAO",
                id,
                0.0,
                1L,
                responsavel != null ? responsavel : "Admin",
                "ADMINISTRACAO",
                String.format("Usuário excluído: %s (Cargo: %s, ID: %d) por %s", nome, cargo, id, responsavel)
        );
        enviarMensagemDireta(dto);
    }

    private static void enviarMensagemDireta(LogOperacaoDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            enviarMensagem(json, QUEUE_USUARIO);
        } catch (Exception e) {
            System.err.println("Erro ao serializar log de Usuário: " + e.getMessage());
        }
    }
}
