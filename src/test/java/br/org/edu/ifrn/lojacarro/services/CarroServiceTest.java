package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.repository.CarroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarroServiceTest { // 🔥 Removido o 'public' da classe

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    @Test
    void deveSalvarCarroComSucesso() { // 🔥 Removido o 'public' do método
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