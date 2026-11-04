package br.org.edu.ifrn.LojaCarro.client;

import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Cliente HTTP responsável pela comunicação da Loja de Carros
 * com o microsserviço ServicoUsuario via API REST.
 * Possui fallback resiliente para a base local (UsuarioRepository) caso o microsserviço esteja offline.
 */
@Component
public class UsuarioClient {

    private static final Logger logger = LogManager.getLogger(UsuarioClient.class);

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${usuario.service.url:http://localhost:8081}")
    private String usuarioServiceUrl;

    public String buscarNomePorId(Long usuarioId) {
        if (usuarioId == null) {
            return "Usuário Desconhecido";
        }
        if (restTemplate != null) {
            try {
                String url = usuarioServiceUrl + "/usuarios/" + usuarioId + "/nome";
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return response.getBody();
                }
            } catch (Exception e) {
                // Fallback para banco local
            }
        }
        return usuarioRepository.findById(usuarioId)
                .map(Usuario::getNome)
                .orElse("Usuário [ID: " + usuarioId + "]");
    }

    public Optional<Usuario> buscarPorId(Long usuarioId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        if (restTemplate != null) {
            try {
                String url = usuarioServiceUrl + "/usuarios/" + usuarioId;
                Usuario usuario = restTemplate.getForObject(url, Usuario.class);
                if (usuario != null) {
                    return Optional.of(usuario);
                }
            } catch (Exception e) {
                // Fallback para banco local
            }
        }
        return usuarioRepository.findById(usuarioId);
    }

    public List<Usuario> listarTodos() {
        if (restTemplate != null) {
            try {
                String url = usuarioServiceUrl + "/usuarios";
                ResponseEntity<List<Usuario>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Usuario>>() {}
                );
                if (response.getBody() != null && !response.getBody().isEmpty()) {
                    return response.getBody();
                }
            } catch (Exception e) {
                // Fallback para banco local
            }
        }
        return usuarioRepository.findAll();
    }

    public Usuario salvar(Usuario usuario) {
        if (restTemplate != null) {
            try {
                String url = usuarioServiceUrl + "/usuarios";
                Usuario salvoRemoto = restTemplate.postForObject(url, usuario, Usuario.class);
                if (salvoRemoto != null) {
                    usuarioRepository.save(salvoRemoto);
                    return salvoRemoto;
                }
            } catch (Exception e) {
                // Fallback para banco local
            }
        }
        return usuarioRepository.save(usuario);
    }

    public void deletar(Long id) {
        if (restTemplate != null) {
            try {
                String url = usuarioServiceUrl + "/usuarios/" + id;
                restTemplate.delete(url);
            } catch (Exception e) {
                // Fallback para banco local
            }
        }
        try {
            usuarioRepository.deleteById(id);
        } catch (Exception ignored) {}
    }

    public Optional<Usuario> autenticar(String login, String senha) {
        if (restTemplate != null) {
            try {
                String url = usuarioServiceUrl + "/usuarios/autenticar";
                Map<String, String> credenciais = new HashMap<>();
                credenciais.put("login", login);
                credenciais.put("senha", senha);

                ResponseEntity<Usuario> response = restTemplate.postForEntity(url, credenciais, Usuario.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return Optional.of(response.getBody());
                }
            } catch (Exception e) {
                // Fallback para banco local
            }
        }
        return usuarioRepository.findByLoginAndSenha(login, senha);
    }
}
