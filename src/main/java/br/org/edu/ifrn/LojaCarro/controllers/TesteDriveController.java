package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.TesteDrive;
import br.org.edu.ifrn.LojaCarro.services.TesteDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teste-drive")
public class TesteDriveController {

    @Autowired
    private TesteDriveService testeDriveService;

    @GetMapping("/listar")
    public ResponseEntity<List<TesteDrive>> listar() {
        return ResponseEntity.ok(testeDriveService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TesteDrive> buscarPorId(@PathVariable Long id) {
        return testeDriveService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = {"", "/salvar"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> salvar(
            @RequestBody TesteDrive testeDrive,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        TesteDrive salvo = testeDriveService.save(testeDrive, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody TesteDrive testeDrive,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        testeDrive.setId(id);
        TesteDrive atualizado = testeDriveService.update(testeDrive, idHeader, nomeHeader, cargoHeader);
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

        testeDriveService.deleteById(id, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.noContent().build();
    }
}
