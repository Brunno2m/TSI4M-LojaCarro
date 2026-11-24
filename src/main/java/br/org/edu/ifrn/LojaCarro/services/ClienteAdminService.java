package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarUsuario;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClienteAdminService {

    private static final Logger logger = LogManager.getLogger(ClienteAdminService.class);
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final EnviarUsuario enviarUsuario;

    public ClienteAdminService(UsuarioService usuarioService,
                               UsuarioRepository usuarioRepository,
                               EnviarUsuario enviarUsuario) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.enviarUsuario = enviarUsuario;
    }

    public Usuario criarCliente(String nome, String email, String senha,
                                Long adminId, String adminNome) {
        // Usa validacoes, normalizacao e BCrypt existentes em UsuarioService.
        Usuario salvo = usuarioService.cadastrarCliente(nome, email, senha);
        // Evento extra identifica quem fez o cadastro administrativo.
        auditar("CADASTRO_ADMINISTRATIVO", salvo, adminId, adminNome);
        return salvo;
    }

    @Transactional
    public void desativarCliente(Long clienteId, Long adminId, String adminNome) {
        if (clienteId == null || clienteId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de cliente invalido.");
        }

        Usuario usuario = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente nao encontrado."));

        if (!"CLIENTE".equals(usuario.getCargo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Administradores nao podem ser desativados por este endpoint.");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            // DELETE repetido e idempotente.
            return;
        }

        // Preserva dados, historico de agendamentos e integridade das FKs.
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
        auditar("DESATIVACAO", usuario, adminId, adminNome);
    }

    private void auditar(String acao, Usuario cliente, Long adminId, String adminNome) {
        String detalhes = acao + " de cliente ID " + cliente.getId()
                + " por administrador ID " + adminId;
        LogOperacaoDTO evento = new LogOperacaoDTO(
                "USUARIO", acao, cliente.getId(), 0.0,
                adminId, adminNome != null ? adminNome : "Administrador", "ADMIN", detalhes);
        try {
            enviarUsuario.enviar(evento);
        } catch (RuntimeException exc) {
            logger.warn("Falha ao enviar auditoria da operacao {} no cliente {}",
                    acao, cliente.getId());
        }
    }
}
