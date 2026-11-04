package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.client.UsuarioClient;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller Web para interface de Usuários na Loja de Carro.
 * Proprietário possui acesso de visualização (apenas leitura).
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private static final Logger logger = LogManager.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioClient usuarioClient;

    @Autowired
    private UsuarioService usuarioService;

    // Lista todos os usuários cadastrados
    @GetMapping
    public String listarUsuarios(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // PROPRIETARIO, SUPERUSUARIO e GERENTE podem ver gerenciamento de usuários
        if (!"PROPRIETARIO".equals(usuarioLogado.getCargo()) &&
            !"SUPERUSUARIO".equals(usuarioLogado.getCargo()) &&
            !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        model.addAttribute("usuarioLogado", usuarioLogado);
        model.addAttribute("usuarios", usuarioClient.listarTodos());
        return "usuarios";
    }

    @GetMapping("/novo")
    public String exibirFormulario(HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Proprietário é apenas leitura
        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/usuarios?somenteLeitura=true";
        }

        if (!"SUPERUSUARIO".equals(usuarioLogado.getCargo()) && !"GERENTE".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        model.addAttribute("usuario", new Usuario());
        model.addAttribute("usuarioLogado", usuarioLogado);

        List<String> cargosPermitidos = new ArrayList<>();
        if ("SUPERUSUARIO".equals(usuarioLogado.getCargo())) {
            cargosPermitidos.add("PROPRIETARIO");
            cargosPermitidos.add("GERENTE");
            cargosPermitidos.add("VENDEDOR");
            cargosPermitidos.add("SECRETARIO");
            cargosPermitidos.add("CONTABIL");
        } else if ("GERENTE".equals(usuarioLogado.getCargo())) {
            cargosPermitidos.add("VENDEDOR");
            cargosPermitidos.add("SECRETARIO");
            cargosPermitidos.add("CONTABIL");
        }

        model.addAttribute("cargosPermitidos", cargosPermitidos);
        return "form-usuario";
    }

    @PostMapping(value = "/salvar", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public String salvarUsuario(Usuario novoUsuario, HttpSession session, Model model) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Proprietário é apenas leitura
        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/usuarios?somenteLeitura=true";
        }

        if ("GERENTE".equals(usuarioLogado.getCargo())) {
            if ("SUPERUSUARIO".equals(novoUsuario.getCargo()) || "GERENTE".equals(novoUsuario.getCargo()) || "PROPRIETARIO".equals(novoUsuario.getCargo())) {
                return "redirect:/usuarios?erroPermissao=true";
            }
        } else if (!"SUPERUSUARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/estoque?semPermissao=true";
        }

        Usuario usuarioSalvo = usuarioService.save(novoUsuario, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());

        logger.info("AÇÃO: Novo Usuário Cadastrado | RESPONSÁVEL: {} ({}) | NOVO: {} (ID: {})",
                usuarioLogado.getNome(), usuarioLogado.getCargo(), usuarioSalvo.getNome(), usuarioSalvo.getId());

        return "redirect:/usuarios?sucesso=true";
    }

    @GetMapping("/deletar/{id}")
    public String deletarUsuario(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Proprietário é apenas leitura
        if ("PROPRIETARIO".equals(usuarioLogado.getCargo())) {
            return "redirect:/usuarios?somenteLeitura=true";
        }

        Optional<Usuario> usuarioOpt = usuarioClient.buscarPorId(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuarioADeletar = usuarioOpt.get();

            if (usuarioADeletar.getId().equals(usuarioLogado.getId())) {
                return "redirect:/usuarios?erroProprioUsuario=true";
            }

            if ("GERENTE".equals(usuarioLogado.getCargo())) {
                if ("SUPERUSUARIO".equals(usuarioADeletar.getCargo()) || "GERENTE".equals(usuarioADeletar.getCargo()) || "PROPRIETARIO".equals(usuarioADeletar.getCargo())) {
                    return "redirect:/usuarios?erroPermissao=true";
                }
            } else if (!"SUPERUSUARIO".equals(usuarioLogado.getCargo())) {
                return "redirect:/estoque?semPermissao=true";
            }

            usuarioService.deleteById(id, usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getCargo());
            logger.info("AÇÃO: Usuário Deletado | RESPONSÁVEL: {} | EXCLUÍDO: {}", usuarioLogado.getNome(), usuarioADeletar.getNome());
        }

        return "redirect:/usuarios?deletado=true";
    }
}
