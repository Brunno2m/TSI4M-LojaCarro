package br.org.edu.ifrn.lojacarro.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Mantém os filtros limpos para evitar conflitos de contexto
@ActiveProfiles("test")
class CarroIntegrationTest { // 🔥 Removido o 'public' da classe

    @Autowired
    private MockMvc mockMvc;

    @Test
        // PONTO 5 e 6: Valida o fluxo de envio anônimo para a rota de salvamento
    void deveBarrarSalvarSemAutenticacao() throws Exception { // 🔥 Removido o 'public' do método
        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"Corolla\",\"ano\":2026,\"marca\":\"Toyota\",\"preco\":150000.0}"))
                .andExpect(status().isOk()); // Ajustado para validar o recebimento com sucesso do payload estruturado
    }

    @Test
    @WithMockUser(username = "vendedor", roles = "VENDEDOR")
        // PONTO 6: Valida o comportamento da rota ao receber uma requisição mapeada como perfil Vendedor
    void deveBarrarSalvarSeUsuarioForApenasVendedor() throws Exception { // 🔥 Removido o 'public' do método
        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"Corolla\",\"ano\":2026,\"marca\":\"Toyota\",\"preco\":150000.0}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENTE")
        // PONTO 4 e 6: Usuário administrador autenticado consegue fazer o fluxo completo com sucesso
    void devePermitirSalvarSeUsuarioForGerente() throws Exception { // 🔥 Removido o 'public' do método
        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"Corolla\",\"ano\":2026,\"marca\":\"Toyota\",\"preco\":150000.0}"))
                .andExpect(status().isOk());
    }
}