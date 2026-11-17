
package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@ConditionalOnProperty(
        name = "app.admin.bootstrap.enabled",
        havingValue = "true"
)
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;

    @Value("${app.admin.nome}")
    private String nome;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.senha}")
    private String senha;

    public AdminInitializer(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {

        Optional<Usuario> existente =
                usuarioService.findByLogin(email);

        if (existente.isPresent()) {

            Usuario usuario = existente.get();

            if (!"ADMIN".equals(usuario.getCargo())
                    || !Boolean.TRUE.equals(usuario.getAtivo())) {
                throw new IllegalStateException(
                        "A conta configurada não é um administrador ativo."
                );
            }

            System.out.println(
                    "Administrador já cadastrado."
            );

            return;
        }

        usuarioService.cadastrarAdministrador(
                nome,
                email,
                senha
        );

        System.out.println(
                "Administrador inicial cadastrado com sucesso."
        );
    }
}
