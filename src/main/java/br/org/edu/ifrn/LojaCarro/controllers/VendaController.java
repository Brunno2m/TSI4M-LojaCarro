package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Venda;
import br.org.edu.ifrn.LojaCarro.services.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @GetMapping("/listar")
    public ResponseEntity<List<Venda>> listar() {
        return ResponseEntity.ok(vendaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venda> buscarPorId(@PathVariable Long id) {
        return vendaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> obterTotalVendas() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("totalFaturamento", vendaService.calcularTotalVendas());
        resp.put("quantidadeVendas", vendaService.findAll().size());
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = {"", "/salvar"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> salvar(
            @RequestBody Venda venda,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        Venda salva = vendaService.save(venda, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.ok(salva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody Venda venda,
            @RequestHeader(value = "X-Usuario-Cargo", required = false) String cargoHeader,
            @RequestHeader(value = "X-Usuario-Nome", required = false) String nomeHeader,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long idHeader) {

        if ("PROPRIETARIO".equalsIgnoreCase(cargoHeader)) {
            return ResponseEntity.status(403).body("Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
        }

        venda.setId(id);
        Venda atualizada = vendaService.update(venda, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.ok(atualizada);
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

        vendaService.deleteById(id, idHeader, nomeHeader, cargoHeader);
        return ResponseEntity.noContent().build();
    }
}
