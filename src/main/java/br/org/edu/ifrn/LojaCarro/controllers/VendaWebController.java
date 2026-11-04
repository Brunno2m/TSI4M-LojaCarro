package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.model.Venda;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.services.VendaService;
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
@RequestMapping("/vendas")
public class VendaWebController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private CarroService carroService;

    @GetMapping
    public String listar(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<Venda> vendas = vendaService.findAll();
        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("vendas", vendas);
        model.addAttribute("totalFaturamento", vendaService.calcularTotalVendas());
        return "vendas";
    }

    @GetMapping("/nova")
    public String novaForm(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/vendas?somenteLeitura=true";
        }

        Venda venda = new Venda();
        venda.setVendedorId(usuarioLogado.getId());
        venda.setVendedorNome(usuarioLogado.getNome());
        venda.setDataVenda(LocalDateTime.now());

        List<Carro> carros = carroService.findAll();

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("venda", venda);
        model.addAttribute("carros", carros);
        return "form-venda";
    }

    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Long id, HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/vendas?somenteLeitura=true";
        }

        Optional<Venda> opt = vendaService.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/vendas";
        }

        List<Carro> carros = carroService.findAll();

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("venda", opt.get());
        model.addAttribute("carros", carros);
        return "form-venda";
    }

    @PostMapping(value = "/salvar", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public String salvar(Venda venda, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/vendas?somenteLeitura=true";
        }

        // Se selecionou carroId, preenche modelo e valor sugerido se não informado
        if (venda.getCarroId() != null) {
            carroService.findById(venda.getCarroId()).ifPresent(c -> {
                venda.setCarroModelo(c.getModelo() + " (" + c.getAno() + ")");
                if (venda.getValorVenda() == null || venda.getValorVenda() <= 0) {
                    venda.setValorVenda(c.getPreco());
                }
            });
        }

        if (venda.getId() != null && venda.getId() > 0) {
            vendaService.update(venda, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        } else {
            if (venda.getVendedorNome() == null || venda.getVendedorNome().trim().isEmpty()) {
                venda.setVendedorNome(usuarioLogado.getNome());
                venda.setVendedorId(usuarioLogado.getId());
            }
            vendaService.save(venda, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        }

        return "redirect:/vendas?sucesso=true";
    }

    @GetMapping("/deletar/{id}")
    public String deletar(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/vendas?somenteLeitura=true";
        }

        vendaService.deleteById(id, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
        return "redirect:/vendas?deletado=true";
    }
}
