package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Contabil;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.ContabilService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/contabil")
public class ContabilWebController {

    @Autowired
    private ContabilService contabilService;

    @GetMapping
    public String listar(HttpSession session,
                         @RequestParam(value = "tipo", required = false) String tipo,
                         Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<Contabil> lancamentos;
        if (tipo != null && !tipo.trim().isEmpty() && !tipo.equalsIgnoreCase("TODOS")) {
            lancamentos = contabilService.findByTipo(tipo.toUpperCase());
        } else {
            lancamentos = contabilService.findAll();
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("lancamentos", lancamentos);
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("totalEntradas", contabilService.calcularTotalEntradas());
        model.addAttribute("totalSaidas", contabilService.calcularTotalSaidas());
        model.addAttribute("saldoAtual", contabilService.calcularSaldoAtual());
        return "contabil";
    }

    @GetMapping("/novo")
    public String novoForm(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Proprietário é estritamente apenas leitura
        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/contabil?somenteLeitura=true";
        }

        Contabil contabil = new Contabil();
        contabil.setResponsavel(usuarioLogado.getNome());
        contabil.setTipo("ENTRADA");

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("contabil", contabil);
        return "form-contabil";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/contabil?somenteLeitura=true";
        }

        Optional<Contabil> opt = contabilService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/contabil";
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("contabil", opt.get());
        return "form-contabil";
    }

    @PostMapping(value = "/salvar", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public String salvar(Contabil contabil, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/contabil?somenteLeitura=true";
        }

        if (contabil.getId() != null && contabil.getId() > 0) {
            contabilService.update(contabil, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        } else {
            contabil.setResponsavel(usuarioLogado.getNome());
            contabilService.save(contabil, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        }

        return "redirect:/contabil?sucesso=true";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/contabil?somenteLeitura=true";
        }

        contabilService.deleteById(id, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        return "redirect:/contabil?deletado=true";
    }
}
