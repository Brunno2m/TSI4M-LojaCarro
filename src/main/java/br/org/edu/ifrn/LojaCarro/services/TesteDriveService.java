package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarTesteDrive;
import br.org.edu.ifrn.LojaCarro.model.TesteDrive;
import br.org.edu.ifrn.LojaCarro.repository.TesteDriveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TesteDriveService {

    @Autowired
    private TesteDriveRepository testeDriveRepository;

    @Autowired
    private EnviarTesteDrive enviarTesteDrive;

    public List<TesteDrive> findAll() {
        return testeDriveRepository.findAllByOrderByDataHoraDesc();
    }

    public List<TesteDrive> findByStatus(String status) {
        return testeDriveRepository.findByStatusOrderByDataHoraDesc(status);
    }

    public Optional<TesteDrive> findById(Long id) {
        if (id == null) return Optional.empty();
        return testeDriveRepository.findById(id);
    }

    public TesteDrive save(TesteDrive testeDrive, Long responsavelId, String responsavelNome, String responsavelCargo) {
        TesteDrive salvo = testeDriveRepository.save(testeDrive);

        String detalhes = String.format("Test Drive Agendado: Cliente %s | Carro: %s | Data: %s | Vendedor: %s",
                salvo.getClienteNome(), salvo.getCarroModelo(), salvo.getDataHora(), salvo.getVendedorResponsavel());

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "CADASTRO",
                salvo.getId(),
                0.0,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "VENDEDOR",
                detalhes
        );
        dto.setCarroId(salvo.getCarroId());
        dto.setCarroModelo(salvo.getCarroModelo());
        enviarTesteDrive.enviar(dto);

        return salvo;
    }

    public TesteDrive update(TesteDrive testeDrive, Long responsavelId, String responsavelNome, String responsavelCargo) {
        TesteDrive atualizado = testeDriveRepository.save(testeDrive);

        String detalhes = String.format("Test Drive Atualizado: ID %d | Cliente %s | Carro: %s | Status: %s por %s",
                atualizado.getId(), atualizado.getClienteNome(), atualizado.getCarroModelo(), atualizado.getStatus(), responsavelNome);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "ATUALIZACAO",
                atualizado.getId(),
                0.0,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "VENDEDOR",
                detalhes
        );
        dto.setCarroId(atualizado.getCarroId());
        dto.setCarroModelo(atualizado.getCarroModelo());
        enviarTesteDrive.enviar(dto);

        return atualizado;
    }

    public void deleteById(Long id, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Optional<TesteDrive> opt = testeDriveRepository.findById(id);
        String cliente = opt.map(TesteDrive::getClienteNome).orElse("ID: " + id);
        String modelo = opt.map(TesteDrive::getCarroModelo).orElse("Desconhecido");

        testeDriveRepository.deleteById(id);

        String detalhes = String.format("Test Drive Cancelado/Excluído: ID %d (Cliente %s, Carro %s) por %s",
                id, cliente, modelo, responsavelNome);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "TESTE_DRIVE",
                "EXCLUSAO",
                id,
                0.0,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "VENDEDOR",
                detalhes
        );
        dto.setCarroModelo(modelo);
        enviarTesteDrive.enviar(dto);
    }
}
