package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarUsuario;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnviarUsuario enviarUsuario;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        if (id == null) return Optional.empty();
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByLogin(String login) {
        return usuarioRepository.findByLogin(login);
    }

    public Optional<Usuario> autenticar(String login, String senha) {
        return usuarioRepository.findByLoginAndSenha(login, senha);
    }

    public String buscarNomePorId(Long id) {
        if (id == null) return "Usuário Desconhecido";
        return usuarioRepository.findById(id)
                .map(Usuario::getNome)
                .orElse("Usuário [ID: " + id + "]");
    }

    public Usuario save(Usuario usuario, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Usuario salvo = usuarioRepository.save(usuario);

        String detalhes = String.format("Usuário cadastrado: %s (Cargo: %s, Login: %s) por %s",
                salvo.getNome(), salvo.getCargo(), salvo.getLogin(), responsavelNome != null ? responsavelNome : "Sistema");

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "CADASTRO",
                salvo.getId(),
                0.0,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "SISTEMA",
                detalhes
        );
        enviarUsuario.enviar(dto);

        return salvo;
    }

    public Usuario update(Usuario usuario, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Usuario atualizado = usuarioRepository.save(usuario);

        String detalhes = String.format("Usuário atualizado: %s (Cargo: %s, Login: %s) por %s",
                atualizado.getNome(), atualizado.getCargo(), atualizado.getLogin(), responsavelNome != null ? responsavelNome : "Sistema");

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "ATUALIZACAO",
                atualizado.getId(),
                0.0,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "SISTEMA",
                detalhes
        );
        enviarUsuario.enviar(dto);

        return atualizado;
    }

    public void deleteById(Long id, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        String nomeExcluido = usuarioOpt.map(Usuario::getNome).orElse("ID: " + id);
        String cargoExcluido = usuarioOpt.map(Usuario::getCargo).orElse("DESCONHECIDO");

        usuarioRepository.deleteById(id);

        String detalhes = String.format("Usuário excluído: %s (Cargo: %s, ID: %d) por %s",
                nomeExcluido, cargoExcluido, id, responsavelNome != null ? responsavelNome : "Sistema");

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                "EXCLUSAO",
                id,
                0.0,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "SISTEMA",
                detalhes
        );
        enviarUsuario.enviar(dto);
    }
}
