package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarUsuario;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EnviarUsuario enviarUsuario;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void testSalvarUsuarioEnviaRabbitMQ() {
        Usuario u = new Usuario("Lucas Silva", "VENDEDOR", "lucas", "123");
        Usuario salvo = new Usuario("Lucas Silva", "VENDEDOR", "lucas", "123");
        salvo.setId(9L);

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(salvo);

        Usuario res = usuarioService.save(u, 1L, "Admin", "SUPERUSUARIO");

        assertNotNull(res);
        assertEquals(9L, res.getId());

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarUsuario).enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();
        assertEquals("USUARIO", log.getEntidade());
        assertEquals("CADASTRO", log.getTipoOperacao());
        assertEquals(9L, log.getReferenciaId());
    }

    @Test
    void testDeletarUsuarioEnviaRabbitMQ() {
        Usuario u = new Usuario("Lucas Silva", "VENDEDOR", "lucas", "123");
        u.setId(9L);

        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(u));

        usuarioService.deleteById(9L, 1L, "Admin", "SUPERUSUARIO");

        verify(usuarioRepository).deleteById(9L);

        ArgumentCaptor<LogOperacaoDTO> captor = ArgumentCaptor.forClass(LogOperacaoDTO.class);
        verify(enviarUsuario).enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();
        assertEquals("USUARIO", log.getEntidade());
        assertEquals("EXCLUSAO", log.getTipoOperacao());
    }
}
