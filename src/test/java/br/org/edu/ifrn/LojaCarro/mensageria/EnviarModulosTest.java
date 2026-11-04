package br.org.edu.ifrn.LojaCarro.mensageria;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnviarModulosTest {

    @Test
    void testInstanciacaoModulosSeparados() {
        EnviarCarro enviarCarro = new EnviarCarro();
        EnviarVenda enviarVenda = new EnviarVenda();
        EnviarContabil enviarContabil = new EnviarContabil();
        EnviarTesteDrive enviarTesteDrive = new EnviarTesteDrive();
        EnviarUsuario enviarUsuario = new EnviarUsuario();

        assertNotNull(enviarCarro);
        assertNotNull(enviarVenda);
        assertNotNull(enviarContabil);
        assertNotNull(enviarTesteDrive);
        assertNotNull(enviarUsuario);

        assertEquals("15.228.215.36", EnviarCarro.HOST);
        assertEquals("15.228.215.36", EnviarVenda.HOST);
        assertEquals("15.228.215.36", EnviarContabil.HOST);
        assertEquals("15.228.215.36", EnviarTesteDrive.HOST);
        assertEquals("15.228.215.36", EnviarUsuario.HOST);

        assertEquals("guilherme_brunno", EnviarCarro.QUEUE_CARRO);
        assertEquals("guilherme_brunno", EnviarVenda.QUEUE_VENDA);
        assertEquals("guilherme_brunno", EnviarContabil.QUEUE_CONTABIL);
        assertEquals("guilherme_brunno", EnviarTesteDrive.QUEUE_TESTE_DRIVE);
        assertEquals("guilherme_brunno", EnviarUsuario.QUEUE_USUARIO);
    }
}
