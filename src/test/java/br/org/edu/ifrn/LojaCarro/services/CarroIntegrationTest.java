package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // Força o uso do application-test.properties que criamos
public class CarroIntegrationTest {

    @Autowired
    private CarroService carroService;

    @Autowired
    private CarroRepository carroRepository;

    // 1. Buscar um ID inexistente
    @Test
    void deveRetornarVazioAoBuscarIdInexistente() {
        Optional<Carro> resultado = carroService.findById(999L);
        assertTrue(resultado.isEmpty(), "O carro não deveria existir no banco");
    }

    // 2. Salvar carro com preço negativo (Espera-se uma falha de validação)
    @Test
    void naoDeveSalvarCarroComPrecoNegativo() {
        Carro carro = new Carro();
        carro.setModelo("Civic");
        carro.setPreco(-5000.0); // Preço inválido

        assertThrows(ConstraintViolationException.class, () -> {
            carroService.save(carro);
        }, "Deveria lançar exceção ao salvar preço negativo");
    }

    // 3. Salvar carro sem modelo (Espera-se uma falha de validação)
    @Test
    void naoDeveSalvarCarroSemModelo() {
        Carro carro = new Carro();
        carro.setPreco(50000.0);
        // Modelo não foi definido (null)

        assertThrows(ConstraintViolationException.class, () -> {
            carroService.save(carro);
        }, "Deveria lançar exceção ao salvar sem modelo");
    }

    // 4. Teste complementar usando @Sql para inserir dados ANTES de executar
    // CORREÇÃO: Adicionamos a coluna 'ano' e o valor '1980' no SQL
    @Test
    @Sql(statements = "INSERT INTO carro (id, modelo, ano, preco) VALUES (100, 'Fusca', 1980, 15000.0)", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = "DELETE FROM carro WHERE id = 100", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deveAtualizarCarroExistente() {
        Optional<Carro> carroExistente = carroService.findById(100L);
        assertTrue(carroExistente.isPresent());

        Carro carroParaAtualizar = carroExistente.get();
        carroParaAtualizar.setPreco(16000.0);

        Carro atualizado = carroService.update(carroParaAtualizar);
        assertEquals(16000.0, atualizado.getPreco());
    }

    // 5. Atualizar um carro inexistente
    // CORREÇÃO: Como o Spring bloqueia atualizar algo que não existe, testamos se ele lança a exceção correta
    @Test
    void deveFalharAoAtualizarCarroInexistente() {
        Carro carro = new Carro();
        carro.setId(888L);
        carro.setModelo("Corolla");
        carro.setAno(2022);
        carro.setPreco(100000.0);

        assertThrows(Exception.class, () -> {
            carroService.update(carro);
        }, "Deveria lançar uma exceção ao tentar atualizar um carro que não existe no banco");
    }
}