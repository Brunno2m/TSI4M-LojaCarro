package br.org.edu.ifrn.lojacarro.services;

import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.repository.CarroRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException; // 🔥 Importação da exceção específica

@Service
public class CarroService {

    private final CarroRepository carroRepository;

    // 🔥 Injeção por Construtor (Corrige o Code Smell apontado na L13)
    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

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
        // 🔥 Substituído por NoSuchElementException (Corrige o Code Smell da L33)
        if (carro.getId() == null || !carroRepository.existsById(carro.getId())) {
            throw new NoSuchElementException("Carro não encontrado para atualização");
        }
        return carroRepository.save(carro);
    }

    public void deleteById(Long id) {
        // 🔥 Substituído por NoSuchElementException (Corrige o Code Smell da L40)
        if (!carroRepository.existsById(id)) {
            throw new NoSuchElementException("Carro não encontrado para exclusão");
        }
        carroRepository.deleteById(id);
    }

    private boolean textoInvalido(String texto) {
        return texto == null || texto.isBlank();
    }
}