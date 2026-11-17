
package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.services.ImagemService;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarCarro;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarroControllerTest {

    @Mock
    private CarroService carroService;

    @Mock
    private ImagemService imagemService;

    @Mock
    private EnviarCarro enviar;

    @Mock
    private LogOperacaoRepository logOperacaoRepository;

    @InjectMocks
    private CarroController carroController;

    private Carro carro;
    private Jwt admin;
    private Jwt cliente;

    @BeforeEach
    void configurar() {

        carro = new Carro();

        carro.setId(1L);
        carro.setMarca("Toyota");
        carro.setModelo("Corolla");
        carro.setAno(2024);
        carro.setAnoModelo(2025);
        carro.setCor("Prata");
        carro.setQuilometragem(15000L);
        carro.setTipoCombustivel("Flex");
        carro.setTipoTransmissao("Automatico");
        carro.setPreco(120000.00);
        carro.setDescricao("Veiculo em excelente estado.");
        carro.setSituacao("DISPONIVEL");
        carro.setFotoPrincipal("corolla.jpg");

        admin = criarToken("ADMIN");
        cliente = criarToken("CLIENTE");
    }

    private Jwt criarToken(String perfil) {

        return Jwt.withTokenValue("token-teste")
                .header("alg", "HS256")
                .subject("1")
                .claim("perfil", perfil)
                .claim("nome", "Usuario Teste")
                .build();
    }

    @Test
    void deveListarCarrosDisponiveis() {

        when(carroService.filtrar(
                null, null, null, null, null
        )).thenReturn(List.of(carro));

        ResponseEntity<List<Carro>> resposta =
                carroController.pesquisarTodosCarros(
                        null, null, null, null, null
                );

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals(1, resposta.getBody().size());
    }

    @Test
    void deveBuscarCarroPorId() {

        when(carroService.findById(1L))
                .thenReturn(Optional.of(carro));

        ResponseEntity<Carro> resposta =
                carroController.pesquisarCarroPorId(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals("Corolla", resposta.getBody().getModelo());
    }

    @Test
    void deveRetornarErroQuandoCarroNaoExiste() {

        when(carroService.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> carroController.pesquisarCarroPorId(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
    }

    @Test
    void naoDeveExibirCarroInativoNoCatalogo() {

        carro.setSituacao("INATIVO");

        when(carroService.findById(1L))
                .thenReturn(Optional.of(carro));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> carroController.pesquisarCarroPorId(1L)
        );

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
    }

    @Test
    void deveCadastrarCarroComoAdministrador() {

        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "corolla.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        when(imagemService.salvar(foto))
                .thenReturn("corolla.jpg");

        when(carroService.save(carro))
                .thenReturn(carro);

        ResponseEntity<Carro> resposta =
                carroController.salvarCarro(carro, foto, admin);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());

        verify(imagemService).salvar(foto);
        verify(carroService).save(carro);
    }

    @Test
    void deveImpedirClienteDeCadastrarCarro() {

        MockMultipartFile foto = new MockMultipartFile(
                "foto",
                "corolla.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> carroController.salvarCarro(
                        carro, foto, cliente
                )
        );

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());

        verifyNoInteractions(carroService, imagemService);
    }

    @Test
    void deveImpedirCadastroSemAutenticacao() {

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> carroController.salvarCarro(
                        carro, null, null
                )
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                erro.getStatusCode()
        );

        verifyNoInteractions(carroService, imagemService);
    }

    @Test
    void deveDesativarCarroComoAdministrador() {

        when(carroService.findById(1L))
                .thenReturn(Optional.of(carro));

        ResponseEntity<Void> resposta =
                carroController.deletarCarro(1L, admin);

        assertEquals(
                HttpStatus.NO_CONTENT,
                resposta.getStatusCode()
        );

        verify(carroService).deleteById(1L);
    }
}
