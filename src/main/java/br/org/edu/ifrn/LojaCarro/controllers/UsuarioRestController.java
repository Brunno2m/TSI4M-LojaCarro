package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuario")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/listar")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = {"", "/salvar"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> salvar(
            @RequestBody Usuario usuario,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        // Proprietário é estritamente apenas leitura
        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        Usuario salvo = usuarioService.save(usuario, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        usuario.setId(id);
        Usuario atualizado = usuarioService.update(usuario, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        usuarioService.deleteById(id, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.noContent().build();
    }
}
