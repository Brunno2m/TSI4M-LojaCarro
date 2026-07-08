package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.dto.CarroDTO; // 🔥 Importação do DTO
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/painel")
public class CarroViewController {

    @Autowired
    private CarroService carroService;

    // 1. Abre a página e carrega a tabela com os carros do banco
    @GetMapping({"", "/", "/carros"})
    public String exibirPainel(Model model) {
        model.addAttribute("listaCarros", carroService.findAll());
        model.addAttribute("carroForm", new CarroDTO()); // 🔥 Mudou para CarroDTO aqui
        return "carros";
    }

    // 2. Recebe os dados do formulário HTML, salva e atualiza a página
    // 🔥 Corrigido para receber CarroDTO (Elimina a vulnerabilidade da L30 apontada pelo Sonar)
    @PostMapping("/salvar")
    public String salvarCarroViaWeb(@ModelAttribute("carroForm") CarroDTO dto) {
        Carro carro = new Carro();
        carro.setModelo(dto.getModelo());
        carro.setMarca(dto.getMarca());
        carro.setAno(dto.getAno());
        carro.setPreco(dto.getPreco());

        carroService.save(carro);
        return "redirect:/painel";
    }
}