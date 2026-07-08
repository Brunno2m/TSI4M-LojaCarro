package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.repository.CarroRepository;
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
class CarroControllerIntegrationTest { // 🔥 Removido o 'public' da classe

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private CarroService carroService;

    // 🔥 Renomeado de URL_BASE para urlBase (Atende a expressao regular exigida pelo Sonar)
    private final String urlBase = "/carro";

    @Test
    void testeAbrirPainelPeloController() throws Exception {
        mockMvc.perform(get("/painel"))
                .andExpect(status().isOk())
                .andExpect(view().name("carros"));
    }

    @Test
    void testeSalvarCarroPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Honda Civic");
        carro.setMarca("Honda");
        carro.setAno(2023);
        carro.setPreco(120000.0);

        mockMvc.perform(post(urlBase + "/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Honda Civic"));

        assertEquals(1, carroRepository.findAll().size(), "Deve haver 1 carro no banco de dados");
    }

    @Test
    void testeBuscarCarroPorIdPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Toyota Corolla");
        carro.setMarca("Toyota");
        carro.setAno(2024);
        carro.setPreco(150000.0);
        Carro carroSalvo = carroRepository.save(carro);

        mockMvc.perform(get(urlBase + "/" + carroSalvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Toyota Corolla"));
    }

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

        mockMvc.perform(get(urlBase))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testeAtualizarCarroPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Ford Ka");
        carro.setMarca("Ford");
        carro.setAno(2019);
        carro.setPreco(45000.0);
        Carro carroSalvo = carroRepository.save(carro);

        carroSalvo.setPreco(42000.0);

        mockMvc.perform(put(urlBase + "/" + carroSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carroSalvo)))
                .andExpect(status().isOk());

        Carro carroNoBanco = carroRepository.findById(carroSalvo.getId()).get();
        assertEquals(42000.0, carroNoBanco.getPreco(), "O preço deve estar atualizado no banco");
    }

    @Test
    void testeExcluirCarroPeloController() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Renault Kwid");
        carro.setMarca("Renault");
        carro.setAno(2023);
        carro.setPreco(60000.0);
        Carro carroSalvo = carroRepository.save(carro);

        mockMvc.perform(delete(urlBase + "/" + carroSalvo.getId()))
                .andExpect(status().isNoContent());

        assertTrue(carroRepository.findById(carroSalvo.getId()).isEmpty(), "O carro não deve mais existir no banco");
    }

    @Test
    void testeFalhaSalvarCarro() {
        Carro carro = new Carro();
        assertThrows(Exception.class, () -> carroService.save(carro));
    }

    @Test
    void testeFalhaBuscarCarroPorId() throws Exception {
        mockMvc.perform(get(urlBase + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testeFalhaListarTodosOsCarros() throws Exception {
        mockMvc.perform(get(urlBase + "/invalido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testeFalhaAtualizarCarro() {
        Carro carro = new Carro();
        carro.setId(999L);
        carro.setMarca("Inexistente");
        assertThrows(Exception.class, () -> carroService.update(carro));
    }

    @Test
    void testeFalhaExcluirCarro() {
        // 🔥 Atualizado para NoSuchElementException para casar exatamente com o novo CarroService
        assertThrows(java.util.NoSuchElementException.class, () -> carroService.deleteById(999L),
                "Deve lançar NoSuchElementException se o carro não for encontrado para exclusão");
    }
}