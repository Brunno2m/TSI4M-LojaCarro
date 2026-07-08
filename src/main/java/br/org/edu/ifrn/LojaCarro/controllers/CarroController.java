package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.dto.CarroDTO; // 🔥 Importação do seu novo DTO
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/carro")
public class CarroController {

    @Autowired
    private CarroService carroService;

    // 🔥 Modificado para receber CarroDTO (Corrige a vulnerabilidade da L19 do Sonar)
    @PostMapping("/salvar")
    public ResponseEntity<Carro> salvarCarro(@RequestBody CarroDTO dto) {
        Carro c = new Carro();
        c.setModelo(dto.getModelo());
        c.setMarca(dto.getMarca());
        c.setAno(dto.getAno());
        c.setPreco(dto.getPreco());

        Carro salvo = carroService.save(c);
        return ResponseEntity.ok(salvo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        return carroService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Carro>> pesquisarTodosCarros() {
        List<Carro> carros = carroService.findAll();
        return ResponseEntity.ok(carros);
    }

    // 🔥 Modificado para receber CarroDTO (Corrige a vulnerabilidade da L39 do Sonar)
    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(@PathVariable Long id, @RequestBody CarroDTO dto) {
        Carro c = new Carro();
        c.setId(id);
        c.setModelo(dto.getModelo());
        c.setMarca(dto.getMarca());
        c.setAno(dto.getAno());
        c.setPreco(dto.getPreco());

        Carro atualizado = carroService.update(c);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id) {
        carroService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}