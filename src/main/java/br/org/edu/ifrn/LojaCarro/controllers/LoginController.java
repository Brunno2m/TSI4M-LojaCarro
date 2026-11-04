package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.client.UsuarioClient;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    private static final Logger logger = LogManager.getLogger(LoginController.class);

    @Autowired
    private UsuarioClient usuarioClient;

    @GetMapping("/")
    public String index(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario != null) {
            if ("PROPRIETARIO".equalsIgnoreCase(usuario.getCargo())) {
                return "redirect:/adm";
            }
            return "redirect:/estoque";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario != null) {
            if ("PROPRIETARIO".equalsIgnoreCase(usuario.getCargo())) {
                return "redirect:/adm";
            }
            return "redirect:/estoque";
        }
        if (error != null) {
            model.addAttribute("erro", "Login ou senha inválidos.");
        }
        if (logout != null) {
            model.addAttribute("mensagem", "Você saiu do sistema com sucesso.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String autenticar(@RequestParam String login,
                             @RequestParam String senha,
                             HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioClient.autenticar(login, senha);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            session.setAttribute("usuarioLogado", usuario);
            logger.info("AÇÃO: Login Efetuado | USUÁRIO: {} (ID: {}) | CARGO: {}",
                    usuario.getNome(), usuario.getId(), usuario.getCargo());

            if ("PROPRIETARIO".equalsIgnoreCase(usuario.getCargo())) {
                return "redirect:/adm";
            }
            return "redirect:/estoque";
        }

        logger.warn("AÇÃO: Tentativa de Login Falhou | LOGIN: {}", login);
        return "redirect:/login?error=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario != null) {
            logger.info("AÇÃO: Logout Efetuado | USUÁRIO: {}", usuario.getLogin());
        }
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
