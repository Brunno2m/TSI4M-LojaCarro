package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.client.UsuarioClient;
import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarCarro;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/estoque")
public class CarroWebController {

    private static final Logger logger = LogManager.getLogger(CarroWebController.class);

    @Autowired
    private CarroService carroService;

    @Autowired
    private UsuarioClient usuarioClient;

    @Autowired
    private EnviarCarro enviar;

    @Autowired
    private LogOperacaoRepository logOperacaoRepository;

    @GetMapping
    public String listarEstoque(HttpSession session,
                                @RequestParam(value = "busca", required = false) String busca,
                                Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<Carro> carros = carroService.findAll();
        if (busca != null && !busca.trim().isEmpty()) {
            carros = carros.stream()
                    .filter(c -> c.getModelo().toLowerCase().contains(busca.trim().toLowerCase()))
                    .toList();
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("carros", carros);
        model.addAttribute("busca", busca);
        return "estoque";
    }

    @GetMapping("/novo")
    public String novoCarroForm(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Proprietário é estritamente apenas leitura
        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?somenteLeitura=true";
        }

        // Apenas SUPERUSUARIO e GERENTE podem cadastrar carros no estoque
        if (!"SUPERUSUARIO".equals(usuarioLogado.getCargo()) && !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("carro", new Carro());
        return "form-carro";
    }

    @GetMapping("/editar/{id}")
    public String editarCarroForm(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?somenteLeitura=true";
        }

        if (!"SUPERUSUARIO".equals(usuarioLogado.getCargo()) && !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        Optional<Carro> carroOpt = carroService.findById(id);
        if (carroOpt.isEmpty()) {
            return "redirect:/estoque";
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("carro", carroOpt.get());
        return "form-carro";
    }

    @PostMapping("/salvar")
    public String salvarCarro(Carro carro, HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?somenteLeitura=true";
        }

        if (!"SUPERUSUARIO".equals(usuarioLogado.getCargo()) && !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        try {
            boolean isEdicao = (carro.getId() != null && carro.getId() > 0);
            Carro carroSalvo;
            String tipoOp = isEdicao ? "ATUALIZACAO" : "CADASTRO";

            if (isEdicao) {
                carroSalvo = carroService.update(carro);
            } else {
                carroSalvo = carroService.save(carro);
            }

            // Comunicação com o ServicoUsuario via API REST:
            String nomeResponsavel = usuarioClient.buscarNomePorId(usuarioLogado.getId());

            logger.info("AÇÃO: Carro {} no Estoque | RESPONSÁVEL: {} (Cargo: {}) [ID: {}] | MODELO: {} | PREÇO: R$ {}",
                    tipoOp, nomeResponsavel, usuarioLogado.getCargo(), usuarioLogado.getId(), carroSalvo.getModelo(), carroSalvo.getPreco());

            // Envio de mensagem assíncrona para o Módulo de Log via RabbitMQ (15.228.215.36)
            String detalhes = String.format("Carro %s no estoque via Web por %s (%s). Modelo: %s, Ano: %d, Preço: R$ %.2f",
                    tipoOp.toLowerCase(), nomeResponsavel, usuarioLogado.getCargo(),
                    carroSalvo.getModelo(), carroSalvo.getAno(), carroSalvo.getPreco());

            LogOperacaoDTO logDTO = new LogOperacaoDTO(
                    tipoOp,
                    carroSalvo.getId(),
                    carroSalvo.getModelo(),
                    carroSalvo.getAno(),
                    carroSalvo.getPreco(),
                    usuarioLogado.getId(),
                    nomeResponsavel,
                    usuarioLogado.getCargo(),
                    detalhes
            );
            logDTO.setEntidade("CARRO");
            enviar.enviar(logDTO);

            return "redirect:/estoque?sucesso=true";
        } catch (CarroException e) {
            model.addAttribute("usuarioLogado", usuarioLogado);
            model.addAttribute("carro", carro);
            model.addAttribute("erro", e.getMessage());
            return "form-carro";
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletarCarro(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?somenteLeitura=true";
        }

        if (!"SUPERUSUARIO".equals(usuarioLogado.getCargo()) && !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        Optional<Carro> carroOpt = carroService.findById(id);
        String modeloCarro = carroOpt.map(Carro::getModelo).orElse("ID: " + id);
        Integer anoCarro = carroOpt.map(Carro::getAno).orElse(null);
        Double precoCarro = carroOpt.map(Carro::getPreco).orElse(null);

        carroService.deleteById(id);

        // Comunicação com o ServicoUsuario via API REST:
        String nomeResponsavel = usuarioClient.buscarNomePorId(usuarioLogado.getId());

        logger.info("AÇÃO: Carro Removido do Estoque | RESPONSÁVEL: {} (Cargo: {}) [ID: {}] | ID_CARRO: {}",
                nomeResponsavel, usuarioLogado.getCargo(), usuarioLogado.getId(), id);

        // Envio de mensagem assíncrona para o Módulo de Log via RabbitMQ (15.228.215.36)
        String detalhes = String.format("Carro excluído do estoque via Web por %s (%s). ID: %d, Modelo: %s",
                nomeResponsavel, usuarioLogado.getCargo(), id, modeloCarro);

        LogOperacaoDTO logDTO = new LogOperacaoDTO(
                "EXCLUSAO",
                id,
                modeloCarro,
                anoCarro,
                precoCarro,
                usuarioLogado.getId(),
                nomeResponsavel,
                usuarioLogado.getCargo(),
                detalhes
        );
        logDTO.setEntidade("CARRO");
        enviar.enviar(logDTO);

        return "redirect:/estoque?deletado=true";
    }

    // Tela de Auditoria de Logs consumidos via RabbitMQ
    @GetMapping("/logs")
    public String exibirLogs(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // PROPRIETARIO, SUPERUSUARIO e GERENTE podem consultar logs de auditoria
        if (!"PROPRIETARIO".equals(usuarioLogado.getCargo()) &&
            !"SUPERUSUARIO".equals(usuarioLogado.getCargo()) &&
            !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        List<LogOperacao> logs = logOperacaoRepository.findAllByOrderByDataHoraDesc();
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("logs", logs);
        return "logs";
    }
}
