
package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarUsuario;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final Logger logger =
            LogManager.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EnviarUsuario enviarUsuario;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Listar usuários
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    // Buscar usuário pelo ID
    public Optional<Usuario> findById(Long id) {

        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return usuarioRepository.findById(id);
    }

    // Buscar usuário pelo e-mail
    public Optional<Usuario> findByLogin(String login) {

        if (login == null || login.isBlank()) {
            return Optional.empty();
        }

        return usuarioRepository.findByLogin(
                normalizarEmail(login)
        );
    }

    // Cadastro público de clientes
    public Usuario cadastrarCliente(
            String nome,
            String email,
            String senha
    ) {

        return cadastrar(
                nome,
                email,
                senha,
                "CLIENTE"
        );
    }

    // Criação interna de administrador
    // Não deve ser exposto em endpoint público
    public Usuario cadastrarAdministrador(
            String nome,
            String email,
            String senha
    ) {

        return cadastrar(
                nome,
                email,
                senha,
                "ADMIN"
        );
    }

    // Cadastro e armazenamento seguro da senha
    private Usuario cadastrar(
            String nome,
            String email,
            String senha,
            String perfil
    ) {

        validarNome(nome);
        validarEmail(email);
        validarSenha(senha);

        email = normalizarEmail(email);

        if (usuarioRepository.findByLogin(email).isPresent()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este e-mail já está cadastrado."
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNome(nome.trim());
        usuario.setLogin(email);
        usuario.setCargo(perfil);
        usuario.setAtivo(true);

        // Armazena apenas o hash BCrypt
        usuario.setSenha(
                passwordEncoder.encode(senha)
        );

        Usuario salvo;

        try {

            salvo = usuarioRepository.save(usuario);

        } catch (DataIntegrityViolationException e) {

            if (usuarioRepository.findByLogin(email).isPresent()) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Este e-mail já está cadastrado."
                );
            }

            throw e;
        }

        registrarLog(
                salvo,
                "CADASTRO",
                "Cadastro de usuário com perfil " + perfil
        );

        return salvo;
    }

    // Autenticação segura com BCrypt
    public Optional<Usuario> autenticar(
            String login,
            String senha
    ) {

        if (login == null || senha == null
                || login.isBlank() || senha.isBlank()
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {

            return Optional.empty();
        }

        Optional<Usuario> usuarioOpt = findByLogin(login);

        if (usuarioOpt.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = usuarioOpt.get();

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            return Optional.empty();
        }

        if (!"ADMIN".equals(usuario.getCargo())
                && !"CLIENTE".equals(usuario.getCargo())) {

            return Optional.empty();
        }

        if (usuario.getSenha() == null
                || !passwordEncoder.matches(
                senha,
                usuario.getSenha()
        )) {

            return Optional.empty();
        }

        registrarLog(
                usuario,
                "LOGIN",
                "Autenticação realizada com sucesso."
        );

        return Optional.of(usuario);
    }

    // Compatibilidade com os serviços existentes
    public String buscarNomePorId(Long id) {

        return findById(id)
                .map(Usuario::getNome)
                .orElse("Usuário desconhecido");
    }

    // Métodos antigos mantidos apenas para compilação

    public Usuario save(
            Usuario usuario,
            Long responsavelId,
            String responsavelNome,
            String responsavelCargo
    ) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Utilize o novo endpoint de cadastro."
        );
    }

    public Usuario update(
            Usuario usuario,
            Long responsavelId,
            String responsavelNome,
            String responsavelCargo
    ) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Operação antiga desativada."
        );
    }

    public void deleteById(
            Long id,
            Long responsavelId,
            String responsavelNome,
            String responsavelCargo
    ) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Operação antiga desativada."
        );
    }

    // Validação do nome
    private void validarNome(String nome) {

        if (nome == null
                || nome.isBlank()
                || nome.trim().length() > 120) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe um nome válido."
            );
        }
    }

    // Validação do e-mail
    private void validarEmail(String email) {

        if (email == null
                || email.trim().length() > 160
                || !email.trim().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
        )) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe um e-mail válido."
            );
        }
    }

    // Validação da senha
    private void validarSenha(String senha) {

        if (senha == null
                || senha.isBlank()
                || senha.length() < 8
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A senha deve possuir pelo menos 8 caracteres e no máximo 72 bytes."
            );
        }
    }

    // Padronização do e-mail
    private String normalizarEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

    // Auditoria via RabbitMQ
    private void registrarLog(
            Usuario usuario,
            String operacao,
            String detalhes
    ) {

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "USUARIO",
                operacao,
                usuario.getId(),
                0.0,
                usuario.getId(),
                usuario.getNome(),
                usuario.getCargo(),
                detalhes
        );

        try {

            enviarUsuario.enviar(dto);

        } catch (Exception e) {

            logger.warn(
                    "Falha no envio da auditoria. Usuário ID: {}",
                    usuario.getId()
            );
        }
    }
}
