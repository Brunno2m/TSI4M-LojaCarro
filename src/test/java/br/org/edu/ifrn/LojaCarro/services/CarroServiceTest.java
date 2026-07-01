package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    @Test
    public void deveSalvarCarroComSucesso() {
        Carro carro = new Carro();
        carro.setModelo("Corolla");
        carro.setMarca("Toyota");
        carro.setAno(2026);
        carro.setPreco(150000.0);


        when(carroRepository.save(any(Carro.class))).thenReturn(carro);

        Carro salvo = carroService.save(carro);

        assertNotNull(salvo);
        verify(carroRepository, times(1)).save(carro);
    }
}