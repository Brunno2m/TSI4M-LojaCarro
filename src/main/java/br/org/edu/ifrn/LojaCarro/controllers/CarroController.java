package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.CarroException;
import br.org.edu.ifrn.LojaCarro.client.UsuarioClient;
import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarCarro;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/carro")
public class CarroController {

    private static final Logger logger = LogManager.getLogger(CarroController.class);

    @Autowired
    private CarroService carroService;

    @Autowired
    private UsuarioClient usuarioClient;

    @Autowired
    private EnviarCarro enviar;

    @Autowired
    private LogOperacaoRepository logOperacaoRepository;

    @Autowired(required = false)
    private HttpServletRequest httpRequest;

    private Long resolverUsuarioId(Long headerId, Long paramId) {
        if (headerId != null) return headerId;
        if (paramId != null) return paramId;
        return 1L; // Usuário padrão para chamadas de teste se omitido
    }

    private void verificarPermissaoLeitura() {
        if (httpRequest != null) {
            String cargo = httpRequest.getHeader("X-Usuario-Cargo");
            if ("PROPRIETARIO".equalsIgnoreCase(cargo)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado: Perfil PROPRIETARIO possui acesso exclusivo de leitura.");
            }
        }
    }

    // Salvar carro (POST)
    @PostMapping("/salvar")
    public ResponseEntity<Carro> salvarCarro(
            @RequestBody Carro c,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestParam(value = "usuarioId", required = false) Long usuarioIdParam) {

        verificarPermissaoLeitura();

        Carro savedCarro = carroService.save(c);

        Long idUsuario = resolverUsuarioId(usuarioIdHeader, usuarioIdParam);
        String nomeUsuario = usuarioClient.buscarNomePorId(idUsuario);

        logger.info("AÇÃO: Carro Cadastrado via API | RESPONSÁVEL: {} [ID: {} via API] | MODELO: {} | PREÇO: R$ {}",
                nomeUsuario, idUsuario, savedCarro.getModelo(), savedCarro.getPreco());

        // Envio de mensagem assíncrona para o Módulo de Log via RabbitMQ (15.228.215.36)
        LogOperacaoDTO logDTO = new LogOperacaoDTO(
                "CADASTRO",
                savedCarro.getId(),
                savedCarro.getModelo(),
                savedCarro.getAno(),
                savedCarro.getPreco(),
                idUsuario,
                nomeUsuario,
                "API",
                "Carro cadastrado via API REST. Modelo: " + savedCarro.getModelo() + ", Ano: " + savedCarro.getAno() + ", Preço: R$ " + savedCarro.getPreco()
        );
        logDTO.setEntidade("CARRO");
        enviar.enviar(logDTO);

        return ResponseEntity.ok(savedCarro);
    }

    // Atualizar carro (por ID)
    @PutMapping("/{id}")
    public ResponseEntity<Carro> atualizarCarro(
            @PathVariable Long id,
            @RequestBody Carro c,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestParam(value = "usuarioId", required = false) Long usuarioIdParam) {

        verificarPermissaoLeitura();

        c.setId(id);
        Carro updatedCarro = carroService.update(c);

        Long idUsuario = resolverUsuarioId(usuarioIdHeader, usuarioIdParam);
        String nomeUsuario = usuarioClient.buscarNomePorId(idUsuario);

        logger.info("AÇÃO: Carro Atualizado via API | RESPONSÁVEL: {} [ID: {} via API] | ID_CARRO: {} | MODELO: {}",
                nomeUsuario, idUsuario, id, updatedCarro.getModelo());

        // Envio de mensagem assíncrona para o Módulo de Log via RabbitMQ (15.228.215.36)
        LogOperacaoDTO logDTO = new LogOperacaoDTO(
                "ATUALIZACAO",
                updatedCarro.getId(),
                updatedCarro.getModelo(),
                updatedCarro.getAno(),
                updatedCarro.getPreco(),
                idUsuario,
                nomeUsuario,
                "API",
                "Carro atualizado via API REST por ID. ID: " + id + ", Modelo: " + updatedCarro.getModelo() + ", Preço: R$ " + updatedCarro.getPreco()
        );
        logDTO.setEntidade("CARRO");
        enviar.enviar(logDTO);

        return ResponseEntity.ok(updatedCarro);
    }

    // Deletar carro (por ID)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestParam(value = "usuarioId", required = false) Long usuarioIdParam) {

        verificarPermissaoLeitura();

        Optional<Carro> carroOpt = carroService.findById(id);
        String modeloCarro = carroOpt.map(Carro::getModelo).orElse("ID: " + id);
        Integer anoCarro = carroOpt.map(Carro::getAno).orElse(null);
        Double precoCarro = carroOpt.map(Carro::getPreco).orElse(null);

        carroService.deleteById(id);

        Long idUsuario = resolverUsuarioId(usuarioIdHeader, usuarioIdParam);
        String nomeUsuario = usuarioClient.buscarNomePorId(idUsuario);

        logger.info("AÇÃO: Carro Deletado via API | RESPONSÁVEL: {} [ID: {} via API] | ID_CARRO: {}",
                nomeUsuario, idUsuario, id);

        // Envio de mensagem assíncrona para o Módulo de Log via RabbitMQ (15.228.215.36)
        LogOperacaoDTO logDTO = new LogOperacaoDTO(
                "EXCLUSAO",
                id,
                modeloCarro,
                anoCarro,
                precoCarro,
                idUsuario,
                nomeUsuario,
                "API",
                "Carro deletado via API REST por ID: " + id + ", Modelo: " + modeloCarro
        );
        logDTO.setEntidade("CARRO");
        enviar.enviar(logDTO);

        return ResponseEntity.noContent().build();
    }

    // Pesquisar carro por ID
    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        Optional<Carro> carro = carroService.findById(id);
        return carro.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Pesquisar todos os carros
    @GetMapping("/listarCarros")
    public ResponseEntity<List<Carro>> pesquisarTodosCarros() {
        List<Carro> carros = carroService.findAll();
        return ResponseEntity.ok(carros);
    }

    // Listar todos os logs de operações salvos pelo Módulo de Log via RabbitMQ
    @GetMapping("/logs")
    public ResponseEntity<List<LogOperacao>> listarLogsOperacoes() {
        List<LogOperacao> logs = logOperacaoRepository.findAllByOrderByDataHoraDesc();
        return ResponseEntity.ok(logs);
    }

    @PostMapping(value = "/getCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> pesquisarCarroPorModelo(@RequestBody String modelo) {
        Optional<Carro> carro = carroService.findByModelo(modelo.trim());
        return carro.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/deleteCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> deletarCarroPorModelo(
            @RequestBody String modelo,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestParam(value = "usuarioId", required = false) Long usuarioIdParam) {
        Carro carro = carroService.deleteByModelo(modelo.trim());

        Long idUsuario = resolverUsuarioId(usuarioIdHeader, usuarioIdParam);
        String nomeUsuario = usuarioClient.buscarNomePorId(idUsuario);

        logger.info("AÇÃO: Carro Deletado por Modelo via API | RESPONSÁVEL: {} [ID: {} via API] | MODELO: {}",
                nomeUsuario, idUsuario, carro.getModelo());

        LogOperacaoDTO logDTO = new LogOperacaoDTO(
                "EXCLUSAO",
                carro.getId(),
                carro.getModelo(),
                carro.getAno(),
                carro.getPreco(),
                idUsuario,
                nomeUsuario,
                "API",
                "Carro deletado via API REST por Modelo: " + carro.getModelo()
        );
        logDTO.setEntidade("CARRO");
        enviar.enviar(logDTO);

        return ResponseEntity.ok("Carro deletado: " + carro.getModelo());
    }

    @PostMapping(value = "/updateCarro", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Carro> atualizarCarroPorModelo(
            @RequestBody String payload,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestParam(value = "usuarioId", required = false) Long usuarioIdParam) {
        String[] partes = payload.split(",", 2);
        if (partes.length < 2) {
            throw new CarroException("Payload inválido. Use modelo,preco.");
        }
        String modelo = partes[0].trim();
        double preco = parsePreco(partes[1].trim());
        Carro updatedCarro = carroService.updateByModelo(modelo, preco);

        Long idUsuario = resolverUsuarioId(usuarioIdHeader, usuarioIdParam);
        String nomeUsuario = usuarioClient.buscarNomePorId(idUsuario);

        logger.info("AÇÃO: Carro Atualizado por Modelo via API | RESPONSÁVEL: {} [ID: {} via API] | MODELO: {} | NOVO PREÇO: R$ {}",
                nomeUsuario, idUsuario, modelo, preco);

        LogOperacaoDTO logDTO = new LogOperacaoDTO(
                "ATUALIZACAO",
                updatedCarro.getId(),
                updatedCarro.getModelo(),
                updatedCarro.getAno(),
                updatedCarro.getPreco(),
                idUsuario,
                nomeUsuario,
                "API",
                "Carro atualizado via API REST por Modelo: " + modelo + ", Novo Preço: R$ " + preco
        );
        logDTO.setEntidade("CARRO");
        enviar.enviar(logDTO);

        return ResponseEntity.ok(updatedCarro);
    }

    @PostMapping(value = "/teste", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> bomDia(@RequestBody String nome) {
        return ResponseEntity.ok("Bom dia, " + nome.trim());
    }

    private double parsePreco(String preco) {
        try {
            return Double.parseDouble(preco);
        } catch (NumberFormatException ex) {
            throw new CarroException("Preço inválido: " + preco);
        }
    }
}
