
package br.org.edu.ifrn.LojaCarro.client;

import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;

import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Component
public class UsuarioClient {

    private final UsuarioService usuarioService;

    public UsuarioClient(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Buscar nome do usuário pelo ID
    public String buscarNomePorId(Long usuarioId) {

        if (usuarioId == null) {
            return "Usuário desconhecido";
        }

        return usuarioService.buscarNomePorId(usuarioId);
    }

    // Buscar usuário pelo ID
    public Optional<Usuario> buscarPorId(Long usuarioId) {

        return usuarioService.findById(usuarioId);
    }

    // Listar usuários
    public List<Usuario> listarTodos() {

        return usuarioService.findAll();
    }

    // Autenticação segura utilizando BCrypt
    public Optional<Usuario> autenticar(
            String login,
            String senha
    ) {

        return usuarioService.autenticar(login, senha);
    }

    // Operação antiga desativada por segurança
    public Usuario salvar(Usuario usuario) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Utilize o novo endpoint de cadastro."
        );
    }

    // Operação antiga desativada por segurança
    public void deletar(Long id) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Operação de exclusão desativada."
        );
    }
}
