package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.*;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmControllerTest {

    @Mock
    private CarroService carroService;

    @Mock
    private VendaService vendaService;

    @Mock
    private ContabilService contabilService;

    @Mock
    private TesteDriveService testeDriveService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private LogOperacaoRepository logOperacaoRepository;

    @InjectMocks
    private AdmController admController;

    @Test
    void testVisaoGeralRetornaModoApenasLeitura() {
        when(carroService.findAll()).thenReturn(Collections.emptyList());
        when(vendaService.findAll()).thenReturn(Collections.emptyList());
        when(contabilService.findAll()).thenReturn(Collections.emptyList());
        when(testeDriveService.findAll()).thenReturn(Collections.emptyList());
        when(usuarioService.findAll()).thenReturn(Collections.emptyList());
        when(logOperacaoRepository.findAllByOrderByDataHoraDesc()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, Object>> resposta = admController.visaoGeral();

        assertNotNull(resposta.getBody());
        assertEquals("PROPRIETARIO_ADM", resposta.getBody().get("perfil"));
        assertEquals("APENAS_LEITURA", resposta.getBody().get("modo"));
    }
}
