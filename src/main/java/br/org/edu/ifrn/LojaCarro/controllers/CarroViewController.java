package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/painel") // Rota que o professor vai acessar no navegador
public class CarroViewController {

    @Autowired
    private CarroService carroService;

    // 1. Abre a página e carrega a tabela com os carros do banco
    @GetMapping({"", "/", "/carros"})
    public String exibirPainel(Model model) {
        model.addAttribute("listaCarros", carroService.findAll());
        model.addAttribute("carroForm", new Carro());
        return "carros";
    }

    // 2. Recebe os dados do formulário HTML, salva e atualiza a página
    @PostMapping("/salvar")
    public String salvarCarroViaWeb(@ModelAttribute("carroForm") Carro carro) {
        carroService.save(carro);
        return "redirect:/painel";
    }
}