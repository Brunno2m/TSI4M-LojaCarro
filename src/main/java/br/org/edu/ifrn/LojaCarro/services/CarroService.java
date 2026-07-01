package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CarroService {

    @Autowired
    private CarroRepository carroRepository;

    public Carro save(Carro carro) {
        if (carro == null || textoInvalido(carro.getModelo()) || textoInvalido(carro.getMarca()) || carro.getPreco() == null || carro.getAno() == null) {
            throw new IllegalArgumentException("Dados inválidos");
        }
        return carroRepository.save(carro);
    }

    public List<Carro> findAll() {
        return carroRepository.findAll();
    }

    public Optional<Carro> findById(Long id) {
        return carroRepository.findById(id);
    }

    public Carro update(Carro carro) {
        if (carro.getId() == null || !carroRepository.existsById(carro.getId())) {
            throw new RuntimeException("Carro não encontrado para atualização");
        }
        return carroRepository.save(carro);
    }

    public void deleteById(Long id) {
        if (!carroRepository.existsById(id)) {
            throw new RuntimeException("Carro não encontrado para exclusão");
        }
        carroRepository.deleteById(id);
    }

    private boolean textoInvalido(String texto) {
        return texto == null || texto.isBlank();
    }
}