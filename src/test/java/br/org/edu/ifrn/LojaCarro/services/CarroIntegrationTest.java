package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // Importante: garante que o banco limpe após cada teste
public class CarroIntegrationTest {

    @Autowired
    private CarroService carroService;

    @Test
    void deveRetornarVazioAoBuscarIdInexistente() {
        Optional<Carro> resultado = carroService.findById(999L);
        assertTrue(resultado.isEmpty());
    }

    // Mudamos para RuntimeException porque é o que configuramos no Service
    @Test
    void naoDeveSalvarCarroComPrecoInvalido() {
        Carro carro = new Carro();
        carro.setModelo("Civic");
        carro.setPreco(-5000.0);

        assertThrows(IllegalArgumentException.class, () -> {
            carroService.save(carro);
        });
    }

    @Test
    void naoDeveSalvarCarroSemModelo() {
        Carro carro = new Carro();
        carro.setPreco(50000.0);

        assertThrows(IllegalArgumentException.class, () -> {
            carroService.save(carro);
        });
    }

    @Test
    @Sql(statements = "INSERT INTO carro (id, modelo, ano, preco) VALUES (100, 'Fusca', 1980, 15000.0)",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void deveAtualizarCarroExistente() {
        Carro carroParaAtualizar = carroService.findById(100L).get();
        carroParaAtualizar.setPreco(16000.0);

        Carro atualizado = carroService.update(carroParaAtualizar);
        assertEquals(16000.0, atualizado.getPreco());
    }

    @Test
    void deveFalharAoAtualizarCarroInexistente() {
        Carro carro = new Carro();
        carro.setId(888L);
        carro.setModelo("Corolla");
        carro.setAno(2022);
        carro.setPreco(100000.0);

        // O seu service original lança RuntimeException quando não encontra o ID
        assertThrows(RuntimeException.class, () -> {
            carroService.update(carro);
        });
    }
}