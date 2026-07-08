package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.CarroDTO;
import br.org.edu.ifrn.lojacarro.model.Carro;
import br.org.edu.ifrn.lojacarro.services.CarroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/painel")
public class CarroViewController {

    private final CarroService carroService;

    // Injeção via Construtor
    public CarroViewController(CarroService carroService) {
        this.carroService = carroService;
    }

    @GetMapping({"", "/", "/carros"})
    public String exibirPainel(Model model) {
        model.addAttribute("listaCarros", carroService.findAll());
        model.addAttribute("carroForm", new CarroDTO());
        return "carros";
    }

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