
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
    public CarroRepository carroRepository;

    public Carro save(Carro c) {
        return carroRepository.save(c);
    }

    // Novo método para deletar por ID
    public void deleteById(Long id) {
        carroRepository.deleteById(id);
    }

    // Novo método para pesquisar por ID
    public Optional<Carro> findById(Long id) {
        return carroRepository.findById(id);
    }

    // Novo método para listar todos os carros
    public List<Carro> findAll() {
        return carroRepository.findAll();
    }

    // Método para atualizar (usa o save existente, mas pode ser renomeado se preferir)
    public Carro update(Carro c) {
        return carroRepository.save(c);  // Retorna o carro salvo para feedback
    }
}