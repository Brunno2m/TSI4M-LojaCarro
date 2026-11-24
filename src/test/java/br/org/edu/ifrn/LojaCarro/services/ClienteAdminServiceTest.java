package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarUsuario;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteAdminServiceTest {

    @Mock private UsuarioService usuarioService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EnviarUsuario enviarUsuario;
    @InjectMocks private ClienteAdminService clienteAdminService;

    @Test
    void criacaoAdminReutilizaCadastroSeguroComPerfilCliente() {
        Usuario cliente = cliente(25L, "CLIENTE", true);
        when(usuarioService.cadastrarCliente("Ana", "ana@teste.com", "SenhaSegura123"))
                .thenReturn(cliente);

        Usuario resultado = clienteAdminService.criarCliente(
                "Ana", "ana@teste.com", "SenhaSegura123", 1L, "Admin");

        assertSame(cliente, resultado);
        assertEquals("CLIENTE", resultado.getCargo());
        verify(usuarioService).cadastrarCliente("Ana", "ana@teste.com", "SenhaSegura123");
        verify(enviarUsuario).enviar(any(LogOperacaoDTO.class));
    }

    @Test
    void desativacaoPreservaUsuarioNoBanco() {
        Usuario cliente = cliente(25L, "CLIENTE", true);
        when(usuarioRepository.findById(25L)).thenReturn(Optional.of(cliente));

        clienteAdminService.desativarCliente(25L, 1L, "Admin");

        assertFalse(cliente.getAtivo());
        verify(usuarioRepository).save(cliente);
        verify(usuarioRepository, never()).delete(any());
        verify(enviarUsuario).enviar(any(LogOperacaoDTO.class));
    }

    @Test
    void naoPermiteDesativarAdministrador() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente(1L, "ADMIN", true)));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> clienteAdminService.desativarCliente(1L, 2L, "Admin"));

        assertEquals(HttpStatus.FORBIDDEN, erro.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoEncontradoRetorna404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> clienteAdminService.desativarCliente(99L, 1L, "Admin"));
        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void desativacaoRepetidaNaoGravaNemAudita() {
        when(usuarioRepository.findById(25L)).thenReturn(Optional.of(cliente(25L, "CLIENTE", false)));
        clienteAdminService.desativarCliente(25L, 1L, "Admin");
        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(enviarUsuario);
    }

    private Usuario cliente(long id, String perfil, boolean ativo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome("Ana Cliente");
        u.setLogin("ana@teste.com");
        u.setCargo(perfil);
        u.setAtivo(ativo);
        return u;
    }
}
