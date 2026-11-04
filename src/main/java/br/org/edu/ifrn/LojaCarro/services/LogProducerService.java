package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.Enviar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Adaptador / Serviço produtor de logs que delega as operações para a classe Enviar
 * do pacote de mensageria.
 */
@Service
public class LogProducerService {

    @Autowired
    private Enviar enviar;

    public void enviarLog(LogOperacaoDTO logDTO) {
        enviar.enviar(logDTO);
    }
}
