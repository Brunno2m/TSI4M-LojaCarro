package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class CarroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private CarroService carroService; // Garanta que o tipo é exatamente o nome da sua classe Service

    // Rota base corrigida para "/carro" (singular)
    private final String URL_BASE = "/carro";

    @Test
    void testeAbrirPainelPeloController() throws Exception {
        mockMvc.perform(get("/painel"))
                .andExpect(status().isOk())
                .andExpect(view().name("carros"));
    }

    // 1. Teste: Salvar Carro -> Usa a rota "/carro/salvar" e espera status 200 (OK)
    @Test
    void testeSalvarCarroPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Honda Civic");
        carro.setMarca("Honda");
        carro.setAno(2023);
        carro.setPreco(120000.0);

        mockMvc.perform(post(URL_BASE + "/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk()) // Corrigido para isOk() conforme o seu Controller
                .andExpect(jsonPath("$.modelo").value("Honda Civic"));

        assertEquals(1, carroRepository.findAll().size(), "Deve haver 1 carro no banco de dados");
    }

    // 2. Teste: Buscar Carro por ID
    @Test
    void testeBuscarCarroPorIdPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Toyota Corolla");
        carro.setMarca("Toyota");
        carro.setAno(2024);
        carro.setPreco(150000.0);
        Carro carroSalvo = carroRepository.save(carro);

        mockMvc.perform(get(URL_BASE + "/" + carroSalvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Toyota Corolla"));
    }

    // 3. Teste: Listar Todos
    @Test
    void testeListarTodosOsCarrosPeloController() throws Exception {
        Carro carro1 = new Carro();
        carro1.setModelo("Fiat Argo");
        carro1.setMarca("Fiat");
        carro1.setAno(2021);
        carro1.setPreco(50000.0);
        carroRepository.save(carro1);

        Carro carro2 = new Carro();
        carro2.setModelo("Jeep Compass");
        carro2.setMarca("Jeep");
        carro2.setAno(2022);
        carro2.setPreco(160000.0);
        carroRepository.save(carro2);

        mockMvc.perform(get(URL_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // 4. Teste: Atualizar Carro -> Usa a rota "/carro/{id}"
    @Test
    void testeAtualizarCarroPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Ford Ka");
        carro.setMarca("Ford");
        carro.setAno(2019);
        carro.setPreco(45000.0);
        Carro carroSalvo = carroRepository.save(carro);

        carroSalvo.setPreco(42000.0);

        // Corrigido: Passando o ID na URL conforme a anotação @PutMapping("/{id}")
        mockMvc.perform(put(URL_BASE + "/" + carroSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carroSalvo)))
                .andExpect(status().isOk());

        Carro carroNoBanco = carroRepository.findById(carroSalvo.getId()).get();
        assertEquals(42000.0, carroNoBanco.getPreco(), "O preço deve estar atualizado no banco");
    }

    // 5. Teste: Excluir Carro
    @Test
    void testeExcluirCarroPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Renault Kwid");
        carro.setMarca("Renault");
        carro.setAno(2023);
        carro.setPreco(60000.0);
        Carro carroSalvo = carroRepository.save(carro);

        mockMvc.perform(delete(URL_BASE + "/" + carroSalvo.getId()))
                .andExpect(status().isNoContent()); // Seu controller retorna noContent(), está correto!

        assertTrue(carroRepository.findById(carroSalvo.getId()).isEmpty(), "O carro não deve mais existir no banco");
    }
    // 1. Falha ao Salvar: Espera erro de validação
    @Test
    void testeFalhaSalvarCarro() {
        Carro carro = new Carro(); // Sem dados, deve falhar
        assertThrows(Exception.class, () -> {
            carroService.save(carro);
        });
    }

    // 2. Falha ao Buscar: Espera erro de ID inexistente
    @Test
    void testeFalhaBuscarCarroPorId() throws Exception {
        mockMvc.perform(get(URL_BASE + "/999"))
                .andExpect(status().isNotFound()); // Este deve passar verde
    }

    // 3. Falha ao Listar (Rota Inválida)
    @Test
    void testeFalhaListarTodosOsCarros() throws Exception {
        mockMvc.perform(get(URL_BASE + "/invalido"))
                .andExpect(status().isBadRequest()); // Este deve passar verde
    }

    // 4. Falha ao Atualizar: ID inexistente
    @Test
    void testeFalhaAtualizarCarro() {
        Carro carro = new Carro();
        carro.setId(999L);
        carro.setMarca("Inexistente");
        // Tenta atualizar ID que não existe, deve lançar exceção
        assertThrows(Exception.class, () -> {
            carroService.update(carro);
        });
    }

       // 5. Falha ao Excluir: ID inexistente
    @Test
    void testeFalhaExcluirCarro() {
        // Valida que o service joga a exceção correta ao tentar deletar o ID 999
        assertThrows(RuntimeException.class, () -> {
            carroService.deleteById(999L);
        }, "Deve lançar RuntimeException se o carro não for encontrado para exclusão");
    }
}