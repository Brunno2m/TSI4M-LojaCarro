package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarContabil;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarVenda;
import br.org.edu.ifrn.LojaCarro.model.Contabil;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.model.Venda;
import br.org.edu.ifrn.LojaCarro.repository.ContabilRepository;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import br.org.edu.ifrn.LojaCarro.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ContabilRepository contabilRepository;

    @Autowired
    private EnviarVenda enviarVenda;

    @Autowired
    private EnviarContabil enviarContabil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Venda> findAll() {
        return vendaRepository.findAllByOrderByDataVendaDesc();
    }

    public Optional<Venda> findById(Long id) {
        if (id == null) return Optional.empty();
        return vendaRepository.findById(id);
    }

    public double calcularTotalVendas() {
        return vendaRepository.findAll().stream()
                .filter(v -> "CONCLUIDA".equalsIgnoreCase(v.getStatus()))
                .mapToDouble(Venda::getValorVenda)
                .sum();
    }

    /**
     * Retorna a taxa de comissão sobre a venda de acordo com o perfil/cargo do usuário:
     * - VENDEDOR: 3% (0.03)
     * - GERENTE: 2% (0.02)
     * - SUPERUSUARIO: 1% (0.01)
     * - Demais perfis (PROPRIETARIO, SECRETARIO, CONTABIL): 0%
     */
    public double obterPercentualComissaoPorCargo(String cargo) {
        if (cargo == null) {
            return 0.0;
        }
        switch (cargo.trim().toUpperCase()) {
            case "VENDEDOR":
                return 0.03;
            case "GERENTE":
                return 0.02;
            case "SUPERUSUARIO":
                return 0.01;
            default:
                return 0.0;
        }
    }

    public Venda save(Venda venda, Long responsavelId, String responsavelNome, String responsavelCargo) {
        if (venda.getDataVenda() == null) {
            venda.setDataVenda(LocalDateTime.now());
        }
        Venda salva = vendaRepository.save(venda);

        // Identifica nome e cargo do responsável pela venda para cálculo da comissão
        String nomeVendedor = (salva.getVendedorNome() != null && !salva.getVendedorNome().trim().isEmpty())
                ? salva.getVendedorNome() : (responsavelNome != null ? responsavelNome : "Vendedor");

        String cargoVendedor = responsavelCargo;
        if ((cargoVendedor == null || cargoVendedor.trim().isEmpty()) && salva.getVendedorId() != null) {
            cargoVendedor = usuarioRepository.findById(salva.getVendedorId())
                    .map(Usuario::getCargo)
                    .orElse(null);
        }
        if (cargoVendedor == null || cargoVendedor.trim().isEmpty()) {
            cargoVendedor = "VENDEDOR";
        }

        double percentual = obterPercentualComissaoPorCargo(cargoVendedor);
        double valorVendaDouble = salva.getValorVenda() != null ? salva.getValorVenda() : 0.0;
        double valorComissao = Math.round((valorVendaDouble * percentual) * 100.0) / 100.0;
        double percentualDisplay = Math.round((percentual * 100.0) * 10.0) / 10.0;

        // 1. Integração automática com o módulo Contábil: gera lançamento de ENTRADA (Recebimento da Venda)
        try {
            Contabil entradaContabil = new Contabil(
                    "Recebimento Venda Veículo: " + salva.getCarroModelo() + " (Cliente: " + salva.getClienteNome() + ")",
                    "ENTRADA",
                    salva.getValorVenda(),
                    "VENDA",
                    nomeVendedor,
                    "Venda ID: " + salva.getId() + ", Pagamento: " + salva.getFormaPagamento()
            );
            contabilRepository.save(entradaContabil);
        } catch (Exception e) {
            System.err.println("Aviso: falha ao espelhar venda no contábil: " + e.getMessage());
        }

        // 2. Integração automática com o módulo Contábil: se houver comissão, gera lançamento de SAÍDA (Comissão)
        if (valorComissao > 0) {
            try {
                Contabil saidaComissao = new Contabil(
                        String.format("Comissão sobre Venda (%.1f%%): %s - %s", percentualDisplay, salva.getCarroModelo(), nomeVendedor),
                        "SAIDA",
                        valorComissao,
                        "COMISSAO",
                        nomeVendedor,
                        String.format("Comissão de %.1f%% referente à Venda ID: %d (%s) - Cargo: %s",
                                percentualDisplay, salva.getId(), salva.getCarroModelo(), cargoVendedor)
                );
                Contabil comissaoSalva = contabilRepository.save(saidaComissao);

                // Emite log contábil via RabbitMQ da comissão gerada
                String detalhesComissao = String.format("Comissão Contábil SAIDA (%.1f%%): R$ %.2f para %s (%s) referente à venda do veículo %s",
                        percentualDisplay, valorComissao, nomeVendedor, cargoVendedor, salva.getCarroModelo());

                LogOperacaoDTO dtoComissao = new LogOperacaoDTO(
                        "CONTABIL",
                        "CADASTRO",
                        comissaoSalva.getId(),
                        valorComissao,
                        responsavelId,
                        nomeVendedor,
                        "CONTABIL",
                        detalhesComissao
                );
                dtoComissao.setPercentualComissao(percentualDisplay);
                dtoComissao.setValorComissao(valorComissao);
                dtoComissao.setCarroId(salva.getCarroId());
                dtoComissao.setCarroModelo(salva.getCarroModelo());
                enviarContabil.enviar(dtoComissao);
            } catch (Exception e) {
                System.err.println("Aviso: falha ao registrar comissão no contábil ou emitir log contábil: " + e.getMessage());
            }
        }

        // 3. Emite log de VENDAS via RabbitMQ (15.228.215.36) com os detalhes da venda e da comissão
        String detalhesVenda;
        if (valorComissao > 0) {
            detalhesVenda = String.format("Venda Concluída: %s | Valor: R$ %.2f | Vendedor: %s (%s) | Comissão (%.1f%%): R$ %.2f | Cliente: %s | Pagamento: %s",
                    salva.getCarroModelo(), salva.getValorVenda(), nomeVendedor, cargoVendedor, percentualDisplay, valorComissao, salva.getClienteNome(), salva.getFormaPagamento());
        } else {
            detalhesVenda = String.format("Venda Concluída: %s | Valor: R$ %.2f | Responsável: %s (%s) | Sem comissão | Cliente: %s | Pagamento: %s",
                    salva.getCarroModelo(), salva.getValorVenda(), nomeVendedor, cargoVendedor, salva.getClienteNome(), salva.getFormaPagamento());
        }

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "VENDAS",
                "CADASTRO",
                salva.getId(),
                salva.getValorVenda(),
                responsavelId,
                nomeVendedor,
                cargoVendedor,
                detalhesVenda
        );
        dto.setCarroId(salva.getCarroId());
        dto.setCarroModelo(salva.getCarroModelo());
        dto.setPercentualComissao(percentualDisplay);
        dto.setValorComissao(valorComissao);
        enviarVenda.enviar(dto);

        return salva;
    }

    public Venda update(Venda venda, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Venda atualizada = vendaRepository.save(venda);

        String detalhes = String.format("Venda Atualizada: ID %d | %s | Valor: R$ %.2f | Status: %s por %s",
                atualizada.getId(), atualizada.getCarroModelo(), atualizada.getValorVenda(), atualizada.getStatus(), responsavelNome);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "VENDAS",
                "ATUALIZACAO",
                atualizada.getId(),
                atualizada.getValorVenda(),
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "VENDEDOR",
                detalhes
        );
        dto.setCarroId(atualizada.getCarroId());
        dto.setCarroModelo(atualizada.getCarroModelo());
        enviarVenda.enviar(dto);

        return atualizada;
    }

    public void deleteById(Long id, Long responsavelId, String responsavelNome, String responsavelCargo) {
        Optional<Venda> opt = vendaRepository.findById(id);
        String modelo = opt.map(Venda::getCarroModelo).orElse("ID: " + id);
        Double valor = opt.map(Venda::getValorVenda).orElse(0.0);

        vendaRepository.deleteById(id);

        String detalhes = String.format("Venda Cancelada/Excluída: ID %d (%s, R$ %.2f) por %s",
                id, modelo, valor, responsavelNome);

        LogOperacaoDTO dto = new LogOperacaoDTO(
                "VENDAS",
                "EXCLUSAO",
                id,
                valor,
                responsavelId,
                responsavelNome != null ? responsavelNome : "Sistema",
                responsavelCargo != null ? responsavelCargo : "VENDEDOR",
                detalhes
        );
        dto.setCarroModelo(modelo);
        enviarVenda.enviar(dto);
    }
}
