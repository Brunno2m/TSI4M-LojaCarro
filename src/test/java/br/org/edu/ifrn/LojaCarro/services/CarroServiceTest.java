package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarroServiceTest {

    @Autowired
    private CarroService carroService;

    @Test
    void testeFalhaSalvarCarroSemDados() {
        Carro carroInvalido = new Carro(); // Objeto vazio
        // Valida que o método save lança um erro ao receber um objeto vazio
        assertThrows(IllegalArgumentException.class, () -> {
            carroService.save(carroInvalido);
        });
    }

    @Test
    void testeFalhaExcluirCarroInexistente() {
        Long idInexistente = 999L;
        // Valida que o sistema "reclama" (lança Exception) ao tentar deletar o que não existe
        assertThrows(RuntimeException.class, () -> {
            carroService.deleteById(idInexistente);
        });
    }

    @Test
    void testeFalhaAtualizarCarroInexistente() {
        Carro carroInexistente = new Carro();
        carroInexistente.setId(999L);
        // Valida que o sistema lança erro ao atualizar id inexistente
        assertThrows(RuntimeException.class, () -> {
            carroService.update(carroInexistente);
        });
    }
}