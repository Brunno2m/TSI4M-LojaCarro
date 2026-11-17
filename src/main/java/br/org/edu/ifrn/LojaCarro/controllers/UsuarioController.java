
package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
import br.org.edu.ifrn.LojaCarro.security.JwtService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public UsuarioController(
            UsuarioService usuarioService,
            JwtService jwtService
    ) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    // Cadastro público de clientes
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(
            @Valid @RequestBody CadastroRequest dados
    ) {

        Usuario usuario = usuarioService.cadastrarCliente(
                dados.nome(),
                dados.email(),
                dados.senha()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(converterUsuario(usuario));
    }

    // Autenticação e geração do JWT
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest dados
    ) {

        Usuario usuario = usuarioService.autenticar(
                dados.email(),
                dados.senha()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos."
        ));

        String token = jwtService.gerarToken(usuario);

        LoginResponse resposta = new LoginResponse(
                token,
                "Bearer",
                3600,
                converterUsuario(usuario)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(resposta);
    }

    // Listagem de clientes exclusiva do administrador
    @GetMapping("/clientes")
    public ResponseEntity<List<UsuarioResponse>> listarClientes() {

        List<UsuarioResponse> clientes =
                usuarioService.findAll()
                        .stream()
                        .filter(usuario ->
                                "CLIENTE".equals(usuario.getCargo()))
                        .map(this::converterUsuario)
                        .toList();

        return ResponseEntity.ok(clientes);
    }

    // Converter entidade em resposta segura
    private UsuarioResponse converterUsuario(Usuario usuario) {

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getCargo(),
                usuario.getAtivo()
        );
    }

    // DTO para cadastro
    public record CadastroRequest(

            @NotBlank
            @Size(max = 120)
            String nome,

            @NotBlank
            @Email
            @Size(max = 160)
            String email,

            @NotBlank
            @Size(min = 8, max = 72)
            String senha

    ) {}

    // DTO para login
    public record LoginRequest(

            @NotBlank
            @Email
            @Size(max = 160)
            String email,

            @NotBlank
            @Size(max = 72)
            String senha

    ) {}

    // DTO de resposta sem senha
    public record UsuarioResponse(
            Long id,
            String nome,
            String email,
            String perfil,
            Boolean ativo
    ) {}

    // DTO de resposta da autenticação
    public record LoginResponse(
            String token,
            String tipo,
            long expiresIn,
            UsuarioResponse usuario
    ) {}
}
