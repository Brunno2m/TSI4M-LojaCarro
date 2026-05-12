package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    @BeforeEach
    void setUp() {
        // Inicializa os mocks antes de cada teste
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveSalvarCarro() {
        Carro carro = new Carro();
        // Configura o comportamento do repositório mockado
        when(carroRepository.save(carro)).thenReturn(carro);

        // Executa o método save do serviço
        Carro resultado = carroService.save(carro);

        // Verifica se não retornou nulo e se o repositório foi chamado
        assertNotNull(resultado);
        verify(carroRepository, times(1)).save(carro);
    }

    @Test
    void deveDeletarPorId() {
        Long id = 1L;
        // Diz para o mock não fazer nada (já que é um método void)
        doNothing().when(carroRepository).deleteById(id);

        // Executa o método deleteById do serviço
        carroService.deleteById(id);

        // Verifica se o deleteById do repositório foi chamado exatamente 1 vez
        verify(carroRepository, times(1)).deleteById(id);
    }

    @Test
    void deveBuscarPorId() {
        Long id = 1L;
        Carro carro = new Carro();
        // Configura o mock para retornar um Optional contendo o carro
        when(carroRepository.findById(id)).thenReturn(Optional.of(carro));

        // Executa o método findById
        Optional<Carro> resultado = carroService.findById(id);

        // Verifica se encontrou algo e se o repositório foi chamado
        assertTrue(resultado.isPresent());
        verify(carroRepository, times(1)).findById(id);
    }

    @Test
    void deveListarTodos() {
        Carro carro1 = new Carro();
        Carro carro2 = new Carro();
        List<Carro> listaDeCarros = Arrays.asList(carro1, carro2);

        // Configura o mock para retornar a lista criada
        when(carroRepository.findAll()).thenReturn(listaDeCarros);

        // Executa o método findAll
        List<Carro> resultado = carroService.findAll();

        // Verifica se a lista retornada tem o tamanho esperado (2 carros)
        assertEquals(99, resultado.size());
        verify(carroRepository, times(1)).findAll();
    }

    @Test
    void deveAtualizarCarro() {
        Carro carro = new Carro();
        // Como o update usa o save do repositório, a lógica é similar ao de salvar
        when(carroRepository.save(carro)).thenReturn(carro);

        // Executa o método update
        Carro resultado = carroService.update(carro);

        // Valida o retorno e a chamada
        assertNotNull(resultado);
        verify(carroRepository, times(1)).save(carro);
    }
}