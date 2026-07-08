package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.model.Carro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CarroIntegrationTestSimples {

    @Autowired
    private CarroService carroService;

    // 1. Teste de Integração: Salvar Carro
    @Test
    void testeSalvarCarroComSucesso() {
        Carro carro = new Carro();
        carro.setModelo("Honda Civic");
        carro.setMarca("Honda");
        carro.setAno(2023);
        carro.setPreco(120000.0);

        Carro carroSalvo = carroService.save(carro);

        // Verifica se o MySQL gerou um ID automaticamente para o carro
        assertNotNull(carroSalvo.getId(), "O ID não deve ser nulo após salvar no banco de dados");
        assertEquals("Honda Civic", carroSalvo.getModelo());
    }

    // 2. Teste de Integração: Buscar Carro por ID
    @Test
    void testeBuscarCarroPorId() {
        // Primeiro, salvamos um carro diretamente no banco
        Carro carro = new Carro();
        carro.setModelo("Toyota Corolla");
        carro.setMarca("Toyota");
        carro.setAno(2024);
        carro.setPreco(150000.0);
        Carro carroSalvo = carroService.save(carro);

        // Em seguida, tentamos buscá-lo pelo ID gerado
        Optional<Carro> carroEncontrado = carroService.findById(carroSalvo.getId());

        assertTrue(carroEncontrado.isPresent(), "O carro deve ser encontrado no banco");
        assertEquals("Toyota Corolla", carroEncontrado.get().getModelo());
    }

    // 3. Teste de Integração: Atualizar Carro
    @Test
    void testeAtualizarCarroComSucesso() {
        // Inserindo o dado original no MySQL
        Carro carro = new Carro();
        carro.setModelo("Chevrolet Onix");
        carro.setMarca("Chevrolet");
        carro.setAno(2020);
        carro.setPreco(60000.0);
        Carro carroSalvo = carroService.save(carro);

        // Modificando os dados
        carroSalvo.setPreco(55000.0);
        Carro carroAtualizado = carroService.update(carroSalvo);

        // Verificando se o MySQL aceitou a atualização
        assertEquals(55000.0, carroAtualizado.getPreco(), "O preço deve ter sido atualizado no banco");
    }

    // 4. Teste de Integração: Listar Todos os Carros
    @Test
    void testeListarTodosOsCarros() {
        // Populando o banco com 2 carros
        Carro carro1 = new Carro();
        carro1.setModelo("Fiat Argo");
        carro1.setMarca("Fiat");
        carro1.setAno(2021);
        carro1.setPreco(50000.0);
        carroService.save(carro1);

        Carro carro2 = new Carro();
        carro2.setModelo("Hyundai HB20");
        carro2.setMarca("Hyundai");
        carro2.setAno(2022);
        carro2.setPreco(70000.0);
        carroService.save(carro2);

        // Listando tudo que está no MySQL
        List<Carro> lista = carroService.findAll();

        assertFalse(lista.isEmpty(), "A lista não deve estar vazia");
        assertTrue(lista.size() >= 2, "A lista deve conter os carros inseridos no banco");
    }

    // 5. Teste de Integração: Excluir Carro
    @Test
    void testeExcluirCarroComSucesso() {
        // Inserindo um carro para depois excluir
        Carro carro = new Carro();
        carro.setModelo("Jeep Renegade");
        carro.setMarca("Jeep");
        carro.setAno(2021);
        carro.setPreco(90000.0);
        Carro carroSalvo = carroService.save(carro);

        // Excluindo o carro do MySQL (Pode ser .delete() ou .deleteById() dependendo de como você nomeou na sua classe)
        carroService.deleteById(carroSalvo.getId());

        // Tentando buscar o carro excluído
        Optional<Carro> carroExcluido = carroService.findById(carroSalvo.getId());
        assertTrue(carroExcluido.isEmpty(), "O carro não deve mais existir no banco após a exclusão");
    }
}