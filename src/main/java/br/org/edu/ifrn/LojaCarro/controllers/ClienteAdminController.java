package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.ClienteAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// GET /auth/clientes continua no UsuarioController original.
@RestController
@RequestMapping("/auth/clientes")
public class ClienteAdminController {

    private final ClienteAdminService clienteAdminService;

    public ClienteAdminController(ClienteAdminService clienteAdminService) {
        this.clienteAdminService = clienteAdminService;
    }

    // Obrigatoriamente protegido por ROLE_ADMIN no SecurityConfig.
    // O perfil nunca e recebido do navegador: sera sempre CLIENTE.
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(
            @Valid @RequestBody CadastroClienteRequest dados,
            @AuthenticationPrincipal Jwt jwt) {
        Long adminId = idAdministrador(jwt);
        Usuario cliente = clienteAdminService.criarCliente(
                dados.nome(), dados.email(), dados.senha(),
                adminId, nomeAdministrador(jwt));
        UsuarioResponse resposta = new UsuarioResponse(
                cliente.getId(), cliente.getNome(), cliente.getLogin(),
                cliente.getCargo(), cliente.getAtivo());
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    // Exclusao logica, sem apagar dados relacionados no banco.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long adminId = idAdministrador(jwt);
        clienteAdminService.desativarCliente(id, adminId, nomeAdministrador(jwt));
        return ResponseEntity.noContent().build();
    }

    private Long idAdministrador(Jwt jwt) {
        if (jwt == null || !"ADMIN".equals(jwt.getClaimAsString("perfil"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente administradores.");
        }
        try {
            long id = Long.parseLong(jwt.getSubject());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException exc) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalido.");
        }
    }

    private String nomeAdministrador(Jwt jwt) {
        String nome = jwt.getClaimAsString("nome");
        return nome != null && !nome.isBlank() ? nome : "Administrador";
    }

    public record CadastroClienteRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8, max = 72) String senha
    ) {}

    public record UsuarioResponse(Long id, String nome, String email,
                                  String perfil, Boolean ativo) {}
}
