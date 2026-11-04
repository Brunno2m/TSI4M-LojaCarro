package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Contabil;
import br.org.edu.ifrn.LojaCarro.services.ContabilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contabil")
public class ContabilController {

    @Autowired
    private ContabilService contabilService;

    @GetMapping("/listar")
    public ResponseEntity<List<Contabil>> listar() {
        return ResponseEntity.ok(contabilService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contabil> buscarPorId(@PathVariable Long id) {
        return contabilService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/saldo")
    public ResponseEntity<Map<String, Object>> obterResumoFinanceiro() {
        Map<String, Object> resumo = new HashMap<>();
        resumo.put("totalEntradas", contabilService.calcularTotalEntradas());
        resumo.put("totalSaidas", contabilService.calcularTotalSaidas());
        resumo.put("saldoAtual", contabilService.calcularSaldoAtual());
        return ResponseEntity.ok(resumo);
    }

    @PostMapping(value = {"", "/salvar"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> salvar(
            @RequestBody Contabil contabil,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        Contabil salvo = contabilService.save(contabil, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody Contabil contabil,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        contabil.setId(id);
        Contabil atualizado = contabilService.update(contabil, idHeader, nomeHeader, cargoHeader);
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

        contabilService.deleteById(id, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.noContent().build();
    }
}
