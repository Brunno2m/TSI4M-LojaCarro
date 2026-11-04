package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarContabil;
import br.org.edu.ifrn.LojaCarro.model.Contabil;
import br.org.edu.ifrn.LojaCarro.repository.ContabilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContabilService {

    @Autowired
    private ContabilRepository contabilRepository;

    @Autowired
    private EnviarContabil enviarContabil;

    public List<Contabil> findAll() {
        return contabilRepository.findAllByOrderByDataLancamentoDesc();
    }

    public List<Contabil> findByTipo(String tipo) {
        return contabilRepository.findByTipoOrderByDataLancamentoDesc(tipo);
    }

    public Optional<Contabil> findById(Long id) {
        if (id == null) return Optional.empty();
        return contabilRepository.findById(id);
    }

    public double calcularTotalEntradas() {
        return contabilRepository.findAll().stream()
                .filter(c -> "ENTRADA".equalsIgnoreCase(c.getTipo()))
                .mapToDouble(Contabil::getValor)
                .sum();
    }

    public double calcularTotalSaidas() {
        return contabilRepository.findAll().stream()
                .filter(c -> "SAIDA".equalsIgnoreCase(c.getTipo()))
                .mapToDouble(Contabil::getValor)
                .sum();
    }

    public double calcularSaldoAtual() {
        return calcularTotalEntradas() - calcularTotalSaidas();
    }

    public Contabil save(Contabil contabil, Long responsavelId, String responsavelNome, String responsavelCargo) {
        if (contabil.getDataLancamento() == null) {
            contabil.setDataLancamento(LocalDateTime.now());
        }
        Contabil salvo = contabilRepository.save(contabil);

        String detalhes = String.format("Lançamento Contábil (%s): %s | Valor: R$ %.2f | Categoria: %s por %s",
                salvo.getTipo(), salvo.getDescricao(), salvo.getValor(), salvo.getCategoria(), responsavelNome);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CONTABIL",
                "CADASTRO",
                salvo.getId(),
                salvo.getValor(),
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "CONTABIL",
                detalhes
        );
        enviarContabil.enviar(dto);

        return salvo;
    }

    public Contabil update(Contabil contabil, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Contabil atualizado = contabilRepository.save(contabil);

        String detalhes = String.format("Lançamento Contábil Atualizado (%s): %s | Valor: R$ %.2f por %s",
                atualizado.getTipo(), atualizado.getDescricao(), atualizado.getValor(), responsavelNome);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CONTABIL",
                "ATUALIZACAO",
                atualizado.getId(),
                atualizado.getValor(),
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "CONTABIL",
                detalhes
        );
        enviarContabil.enviar(dto);

        return atualizado;
    }

    public void deleteById(Long id, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Optional<Contabil> opt = contabilRepository.findById(id);
        String desc = opt.map(Contabil::getDescricao).orElse("ID: " + id);
        Double valor = opt.map(Contabil::getValor).orElse(0.0);

        contabilRepository.deleteById(id);

        String detalhes = String.format("Lançamento Contábil Excluído: ID %d (%s, R$ %.2f) por %s",
                id, desc, valor, responsavelNome);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "CONTABIL",
                "EXCLUSAO",
                id,
                valor,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "CONTABIL",
                detalhes
        );
        enviarContabil.enviar(dto);
    }
}
