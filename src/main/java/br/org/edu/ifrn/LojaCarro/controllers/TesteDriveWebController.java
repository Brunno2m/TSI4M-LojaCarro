package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.TesteDrive;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.services.TesteDriveService;
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
@RequestMapping("/teste-drive")
public class TesteDriveWebController {

    @Autowired
    private TesteDriveService testeDriveService;

    @Autowired
    private CarroService carroService;

    @GetMapping
    public String listar(HttpSession session,
                         @RequestParam(value = "status", required = false) String status,
                         Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<TesteDrive> testes;
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("TODOS")) {
            testes = testeDriveService.findByStatus(status.toUpperCase());
        } else {
            testes = testeDriveService.findAll();
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("testes", testes);
        model.addAttribute("statusFiltro", status);
        return "teste-drive";
    }

    @GetMapping("/novo")
    public String novoForm(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/teste-drive?somenteLeitura=true";
        }

        TesteDrive testeDrive = new TesteDrive();
        testeDrive.setVendedorResponsavel(usuarioLogado.getNome());
        testeDrive.setDataHora(LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0));

        List<Carro> carros = carroService.findAll();

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("testeDrive", testeDrive);
        model.addAttribute("carros", carros);
        return "form-teste-drive";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/teste-drive?somenteLeitura=true";
        }

        Optional<TesteDrive> opt = testeDriveService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/teste-drive";
        }

        List<Carro> carros = carroService.findAll();

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("testeDrive", opt.get());
        model.addAttribute("carros", carros);
        return "form-teste-drive";
    }

    @PostMapping(value = "/salvar", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public String salvar(TesteDrive testeDrive, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/teste-drive?somenteLeitura=true";
        }

        // Se carroId foi selecionado, sincroniza carroModelo
        if (testeDrive.getCarroId() != null) {
            carroService.findById(testeDrive.getCarroId()).ifPresent(c -> {
                testeDrive.setCarroModelo(c.getModelo() + " (" + c.getAno() + ")");
            });
        }

        if (testeDrive.getId() != null && testeDrive.getId() > 0) {
            testeDriveService.update(testeDrive, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        } else {
            if (testeDrive.getVendedorResponsavel() == null || testeDrive.getVendedorResponsavel().trim().isEmpty()) {
                testeDrive.setVendedorResponsavel(usuarioLogado.getNome());
            }
            testeDriveService.save(testeDrive, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        }

        return "redirect:/teste-drive?sucesso=true";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/teste-drive?somenteLeitura=true";
        }

        testeDriveService.deleteById(id, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        return "redirect:/teste-drive?deletado=true";
    }
}
