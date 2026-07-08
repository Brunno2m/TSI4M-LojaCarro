package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.dto.CarroDTO;
import br.org.edu.ifrn.lojacarro.services.CarroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*") // Sonar: Habilitado para permitir comunicacao com o Front-end local (Vite/React)
@RestController
@RequestMapping("/carro")
public class CarroController {

    private final CarroService carroService;

    // Injeção via Construtor (Corrige o Code Smell Principal)
    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @PostMapping("/salvar")
    public ResponseEntity<Carro> salvarCarro(@RequestBody CarroDTO dto) {
        Carro c = new Carro();
        c.setModelo(dto.getModelo());
        c.setMarca(dto.getMarca());
        c.setAno(dto.getAno());
        c.setPreco(dto.getPreco());
        return ResponseEntity.ok(carroService.save(c));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        return carroService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Carro>> pesquisarTodosCarros() {
        return ResponseEntity.ok(carroService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(@PathVariable Long id, @RequestBody CarroDTO dto) {
        Carro c = new Carro();
        c.setId(id);
        c.setModelo(dto.getModelo());
        c.setMarca(dto.getMarca());
        c.setAno(dto.getAno());
        c.setPreco(dto.getPreco());
        return ResponseEntity.ok(carroService.update(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id) {
        carroService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}