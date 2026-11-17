package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.dto.LogOperacaoDTO;
import br.org.edu.ifrn.LojaCarro.mensageria.EnviarCarro;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.LogOperacao;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.services.ImagemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/carro")
public class CarroController {

    private static final Logger logger = LogManager.getLogger(CarroController.class);
    private final CarroService carroService;
    private final ImagemService imagemService;
    private final EnviarCarro enviar;
    private final LogOperacaoRepository logOperacaoRepository;

    public CarroController(CarroService carroService, ImagemService imagemService,
                           EnviarCarro enviar, LogOperacaoRepository logOperacaoRepository) {
        this.carroService = carroService;
        this.imagemService = imagemService;
        this.enviar = enviar;
        this.logOperacaoRepository = logOperacaoRepository;
    }

    // Catálogo público: somente veículos disponíveis.
    @GetMapping({"", "/listarCarros"})
    public ResponseEntity<List<Carro>> pesquisarTodosCarros(
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) Double precoMinimo,
            @RequestParam(required = false) Double precoMaximo,
            @RequestParam(required = false) Integer anoModelo) {
        return ResponseEntity.ok(carroService.filtrar(
                marca, modelo, precoMinimo, precoMaximo, anoModelo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carro> pesquisarCarroPorId(@PathVariable Long id) {
        return ResponseEntity.ok(buscarDisponivel(id));
    }

    @GetMapping("/{id}/foto")
    public ResponseEntity<Resource> foto(@PathVariable Long id) {
        return responderFoto(buscarDisponivel(id));
    }

    // Acesso administrativo: exige JWT com perfil ADMIN.
    @GetMapping("/admin")
    public List<Carro> listarAdministrativo(@AuthenticationPrincipal Jwt jwt) {
        exigirAdmin(jwt);
        return carroService.findAll();
    }

    @GetMapping("/admin/{id}")
    public Carro detalhesAdministrativos(@PathVariable Long id,
                                         @AuthenticationPrincipal Jwt jwt) {
        exigirAdmin(jwt);
        return buscarCarro(id);
    }

    @GetMapping("/admin/{id}/foto")
    public ResponseEntity<Resource> fotoAdministrativa(@PathVariable Long id,
                                                       @AuthenticationPrincipal Jwt jwt) {
        exigirAdmin(jwt);
        return responderFoto(buscarCarro(id));
    }

    @PostMapping(value = {"", "/salvar"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Carro> salvarCarro(
            @RequestPart("dados") Carro dados,
            @RequestPart("foto") MultipartFile foto,
            @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = exigirAdmin(jwt);
        String caminho = imagemService.salvar(foto);
        dados.setFotoPrincipal(caminho); // Nunca aceita um caminho informado pelo cliente.
        Carro salvo;
        try {
            salvo = carroService.save(dados);
        } catch (RuntimeException e) {
            imagemService.apagarNovoArquivo(caminho);
            throw e;
        }
        auditar("CADASTRO", salvo, jwt, usuarioId);
        return ResponseEntity.created(URI.create("/carro/admin/" + salvo.getId())).body(salvo);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Carro> atualizarCarro(
            @PathVariable Long id,
            @RequestPart("dados") Carro dados,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = exigirAdmin(jwt);
        Carro anterior = buscarCarro(id);
        dados.setId(id);
        // Preserva a foto persistida caso não exista um novo envio.
        dados.setFotoPrincipal(null);
        String caminho = null;
        if (foto != null) {
            caminho = imagemService.salvar(foto);
            dados.setFotoPrincipal(caminho);
        }
        Carro atualizado;
        try {
            if (caminho == null) {
                imagemService.carregar(anterior.getFotoPrincipal());
            }
            atualizado = carroService.update(dados);
        } catch (RuntimeException e) {
            if (caminho != null) imagemService.apagarNovoArquivo(caminho);
            throw e;
        }
        // A fotografia antiga é preservada no disco por enquanto.
        auditar("ATUALIZACAO", atualizado, jwt, usuarioId);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCarro(@PathVariable Long id,
                                             @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = exigirAdmin(jwt);
        Carro carro = buscarCarro(id);
        carroService.deleteById(id);
        auditar("EXCLUSAO_LOGICA", carro, jwt, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/vender")
    public Carro marcarComoVendido(@PathVariable Long id,
                                   @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = exigirAdmin(jwt);
        Carro carro = carroService.marcarComoVendido(id);
        auditar("VENDA", carro, jwt, usuarioId);
        return carro;
    }

    @GetMapping("/logs")
    public List<LogOperacao> listarLogsOperacoes(@AuthenticationPrincipal Jwt jwt) {
        exigirAdmin(jwt);
        return logOperacaoRepository.findAllByOrderByDataHoraDesc();
    }

    private Carro buscarCarro(Long id) {
        return carroService.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Veículo não encontrado."));
    }

    private Carro buscarDisponivel(Long id) {
        Carro carro = buscarCarro(id);
        if (!Objects.equals("DISPONIVEL", carro.getSituacao())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Veículo não encontrado.");
        }
        return carro;
    }

    private ResponseEntity<Resource> responderFoto(Carro carro) {
        String caminho = carro.getFotoPrincipal();
        return ResponseEntity.ok()
                .contentType(imagemService.tipo(caminho))
                .header("X-Content-Type-Options", "nosniff")
                .body(imagemService.carregar(caminho));
    }

    // Contrato para a autenticação que implementaremos: sub = ID do usuário;
    // perfil = ADMIN ou CLIENTE; nome = nome do usuário.
    private Long exigirAdmin(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login.");
        }
        if (!"ADMIN".equals(jwt.getClaimAsString("perfil"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito ao administrador.");
        }
        try {
            long id = Long.parseLong(jwt.getSubject());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido.");
        }
    }

    private void auditar(String acao, Carro carro, Jwt jwt, Long usuarioId) {
        String nome = jwt.getClaimAsString("nome");
        if (nome == null) nome = "Administrador";
        String detalhes = acao + " do veículo " + carro.getId()
                + ": " + carro.getModelo();
        logger.info("AÇÃO: {} | USUÁRIO: {} | VEÍCULO: {}", acao, usuarioId, carro.getId());
        LogOperacaoDTO dto = new LogOperacaoDTO(acao, carro.getId(), carro.getModelo(),
                carro.getAno(), carro.getPreco(), usuarioId, nome, "ADMIN", detalhes);
        dto.setEntidade("CARRO");
        try {
            enviar.enviar(dto);
        } catch (RuntimeException e) {
            // O CRUD já foi confirmado; não informa falsamente que a gravação falhou.
            // Entrega garantida da auditoria será tratada no RF07.
            logger.warn("Não foi possível enviar auditoria ao RabbitMQ para veículo {}", carro.getId());
        }
    }
}
