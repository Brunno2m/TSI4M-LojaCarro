
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EnviarUsuario enviarUsuario;

    @Spy
    private PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @InjectMocks
    private UsuarioService usuarioService;

    // Teste 1: Cadastro de cliente com BCrypt e auditoria
    @Test
    void testCadastrarClienteComBCrypt() {

        String senha = "ClienteTeste123";

        when(usuarioRepository.findByLogin("cliente@teste.com"))
                .thenReturn(Optional.empty());

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {

                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(9L);

                    return usuario;
                });

        Usuario salvo = usuarioService.cadastrarCliente(
                "Cliente Teste",
                " Cliente@Teste.com ",
                senha
        );

        assertNotNull(salvo);
        assertEquals(9L, salvo.getId());
        assertEquals("CLIENTE", salvo.getCargo());
        assertEquals("cliente@teste.com", salvo.getLogin());
        assertTrue(salvo.getAtivo());

        // Verifica a senha enviada ao repositorio
        ArgumentCaptor<Usuario> usuarioCaptor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository)
                .save(usuarioCaptor.capture());

        Usuario persistido = usuarioCaptor.getValue();

        assertNotEquals(senha, persistido.getSenha());

        assertTrue(
                passwordEncoder.matches(
                        senha,
                        persistido.getSenha()
                )
        );

        // Verifica o evento de auditoria
        ArgumentCaptor<LogOperacaoDTO> captor =
                ArgumentCaptor.forClass(LogOperacaoDTO.class);

        verify(enviarUsuario).enviar(captor.capture());

        LogOperacaoDTO log = captor.getValue();

        assertEquals("USUARIO", log.getEntidade());
        assertEquals("CADASTRO", log.getTipoOperacao());
        assertEquals(9L, log.getReferenciaId());
    }

    // Teste 2: Bloqueio de email duplicado
    @Test
    void testBloquearEmailDuplicado() {

        Usuario existente = new Usuario();

        existente.setLogin("cliente@teste.com");

        when(usuarioRepository.findByLogin("cliente@teste.com"))
                .thenReturn(Optional.of(existente));

        ResponseStatusException erro = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.cadastrarCliente(
                        "Outro Cliente",
                        "cliente@teste.com",
                        "SenhaTeste123"
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                erro.getStatusCode()
        );

        verify(usuarioRepository, never())
                .save(any(Usuario.class));

        verifyNoInteractions(enviarUsuario);
    }

    // Teste 3: Autenticacao com senha correta
    @Test
    void testAutenticarComSenhaCorreta() {

        Usuario usuario = criarUsuarioTeste(true);

        when(usuarioRepository.findByLogin("cliente@teste.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.autenticar(
                        "cliente@teste.com",
                        "ClienteTeste123"
                );

        assertTrue(resultado.isPresent());
        assertEquals(9L, resultado.get().getId());
        assertEquals("CLIENTE", resultado.get().getCargo());

        ArgumentCaptor<LogOperacaoDTO> captor =
                ArgumentCaptor.forClass(LogOperacaoDTO.class);

        verify(enviarUsuario).enviar(captor.capture());

        assertEquals(
                "LOGIN",
                captor.getValue().getTipoOperacao()
        );
    }

    // Teste 4: Rejeicao de senha incorreta
    @Test
    void testRejeitarSenhaIncorreta() {

        Usuario usuario = criarUsuarioTeste(true);

        when(usuarioRepository.findByLogin("cliente@teste.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.autenticar(
                        "cliente@teste.com",
                        "SenhaIncorreta123"
                );

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(enviarUsuario);
    }

    // Teste 5: Bloqueio de usuario inativo
    @Test
    void testBloquearUsuarioInativo() {

        Usuario usuario = criarUsuarioTeste(false);

        when(usuarioRepository.findByLogin("cliente@teste.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado =
                usuarioService.autenticar(
                        "cliente@teste.com",
                        "ClienteTeste123"
                );

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(enviarUsuario);
    }

    // Teste 6: Bloqueio das operacoes antigas
    @Test
    void testBloquearOperacoesLegadas() {

        Usuario usuario = criarUsuarioTeste(true);

        ResponseStatusException erroCadastro = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.save(
                        usuario,
                        1L,
                        "Administrador",
                        "ADMIN"
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                erroCadastro.getStatusCode()
        );

        ResponseStatusException erroAtualizacao = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.update(
                        usuario,
                        1L,
                        "Administrador",
                        "ADMIN"
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                erroAtualizacao.getStatusCode()
        );

        ResponseStatusException erroExclusao = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.deleteById(
                        9L,
                        1L,
                        "Administrador",
                        "ADMIN"
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                erroExclusao.getStatusCode()
        );

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(enviarUsuario);
    }

    // Metodo auxiliar para criar usuarios de teste
    private Usuario criarUsuarioTeste(boolean ativo) {

        Usuario usuario = new Usuario();

        usuario.setId(9L);
        usuario.setNome("Cliente Teste");
        usuario.setLogin("cliente@teste.com");
        usuario.setCargo("CLIENTE");
        usuario.setAtivo(ativo);

        usuario.setSenha(
                passwordEncoder.encode("ClienteTeste123")
        );

        return usuario;
    }
}
