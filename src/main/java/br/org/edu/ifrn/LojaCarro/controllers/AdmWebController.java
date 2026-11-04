package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.*;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller Web para a interface do Proprietário (ADM).
 * Consolida todas as tabelas e dados da loja em modo estritamente LEITURA.
 */
@Controller
@RequestMapping("/adm")
public class AdmWebController {

    @Autowired
    private CarroService carroService;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ContabilService contabilService;

    @Autowired
    private TesteDriveService testeDriveService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LogOperacaoRepository logOperacaoRepository;

    @GetMapping
    public String painelProprietario(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Permite PROPRIETARIO, SUPERUSUARIO e GERENTE visualizarem
        if (!"PROPRIETARIO".equals(usuarioLogado.getCargo()) &&
            !"SUPERUSUARIO".equals(usuarioLogado.getCargo()) &&
            !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        List<Carro> carros = carroService.findAll();
        List<Venda> vendas = vendaService.findAll();
        List<Contabil> lancamentos = contabilService.findAll();
        List<TesteDrive> testes = testeDriveService.findAll();
        List<Usuario> usuarios = usuarioService.findAll();
        List<LogOperacao> logs = logOperacaoRepository.findAllByOrderByDataHoraDesc();

        double valorTotalEstoque = carros.stream().mapToDouble(Carro::getPreco).sum();
        double totalVendas = vendaService.calcularTotalVendas();
        double totalEntradas = contabilService.calcularTotalEntradas();
        double totalSaidas = contabilService.calcularTotalSaidas();
        double saldoContabil = contabilService.calcularSaldoAtual();

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("carros", carros);
        model.addAttribute("vendas", vendas);
        model.addAttribute("lancamentos", lancamentos);
        model.addAttribute("testes", testes);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("logs", logs);

        model.addAttribute("valorTotalEstoque", valorTotalEstoque);
        model.addAttribute("totalVendas", totalVendas);
        model.addAttribute("totalEntradas", totalEntradas);
        model.addAttribute("totalSaidas", totalSaidas);
        model.addAttribute("saldoContabil", saldoContabil);

        return "adm";
    }
}
