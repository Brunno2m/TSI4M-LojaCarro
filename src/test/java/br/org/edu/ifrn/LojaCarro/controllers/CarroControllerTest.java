package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.client.UsuarioClient;
import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarCarro;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarroControllerTest {

    @Mock
    private CarroService carroService;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private EnviarCarro enviar;

    @InjectMocks
    private CarroController carroController;

    @Test
    void testSalvarCarroEnviaMensagemRabbitMQ() {
        Carro entrada = new Carro("Polo", 2024, 85000.0);
        Carro salvo = new Carro("Polo", 2024, 85000.0);
        salvo.setId(10L);

        when(carroService.save(any(Carro.class))).thenReturn(salvo);
        when(usuarioClient.buscarNomePorId(2L)).thenReturn("Carlos Gerente");

        ResponseEntity<Carro> resposta = carroController.salvarCarro(entrada, 2L, null);

        assertNotNull(resposta.getBody());
        assertEquals(10L, resposta.getBody().getId());

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviar).enviar(captor.capture());

        LogOperacaoDTO logEnviado = captor.getValue();
        assertEquals("CADASTRO", logEnviado.getTipoOperacao());
        assertEquals("Polo", logEnviado.getCarroModelo());
        assertEquals(85000.0, logEnviado.getCarroPreco());
        assertEquals("Carlos Gerente", logEnviado.getUsuarioNome());
    }

    @Test
    void testAtualizarCarroEnviaMensagemRabbitMQ() {
        Carro entrada = new Carro("Polo", 2024, 89000.0);
        Carro atualizado = new Carro("Polo", 2024, 89000.0);
        atualizado.setId(10L);

        when(carroService.update(any(Carro.class))).thenReturn(atualizado);
        when(usuarioClient.buscarNomePorId(1L)).thenReturn("Admin");

        ResponseEntity<Carro> resposta = carroController.atualizarCarro(10L, entrada, 1L, null);

        assertNotNull(resposta.getBody());
        assertEquals(10L, resposta.getBody().getId());

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviar).enviar(captor.capture());

        LogOperacaoDTO logEnviado = captor.getValue();
        assertEquals("ATUALIZACAO", logEnviado.getTipoOperacao());
        assertEquals(10L, logEnviado.getCarroId());
        assertEquals(89000.0, logEnviado.getCarroPreco());
    }

    @Test
    void testDeletarCarroEnviaMensagemRabbitMQ() {
        Carro carroExistente = new Carro("Ka", 2021, 42000.0);
        carroExistente.setId(3L);

        when(carroService.findById(3L)).thenReturn(Optional.of(carroExistente));
        when(usuarioClient.buscarNomePorId(1L)).thenReturn("Admin");

        ResponseEntity<Void> resposta = carroController.deletarCarro(3L, 1L, null);

        assertEquals(204, resposta.getStatusCode().value());
        verify(carroService).deleteById(3L);

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviar).enviar(captor.capture());

        LogOperacaoDTO logEnviado = captor.getValue();
        assertEquals("EXCLUSAO", logEnviado.getTipoOperacao());
        assertEquals(3L, logEnviado.getCarroId());
        assertEquals("Ka", logEnviado.getCarroModelo());
    }
}
