package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.*;
import br.org.edu.ifrn.LojaCarro.repository.LogOperacaoRepository;
import br.org.edu.ifrn.LojaCarro.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST Administrativo para o Proprietário (ADM).
 * Conforme especificado, o ADM possui acesso ESTRITAMENTE DE LEITURA
 * sobre todas as demais entidades do sistema.
 */
@RestController
@RequestMapping("/adm")
public class AdmController {

    @Autowired
    private CarroService carroService;

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ContabilService contabilService;

    @Autowired
    private TesteDriveService testeDriveService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LogOperacaoRepository logOperacaoRepository;

    /**
     * Visão Geral Executiva consolidando todas as entidades da loja.
     */
    @GetMapping("/visao-geral")
    public ResponseEntity<Map<String, Object>> visaoGeral() {
        Map<String, Object> visao = new HashMap<>();

        List<Carro> carros = carroService.findAll();
        List<Venda> vendas = vendaService.findAll();
        List<Contabil> lancamentos = contabilService.findAll();
        List<TesteDrive> testes = testeDriveService.findAll();
        List<Usuario> usuarios = usuarioService.findAll();
        List<LogOperacao> logs = logOperacaoRepository.findAllByOrderByDataHoraDesc();

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalCarrosEstoque", carros.size());
        kpis.put("valorTotalEstoque", carros.stream().mapToDouble(Carro::getPreco).sum());
        kpis.put("totalVendasRealizadas", vendas.size());
        kpis.put("faturamentoTotalVendas", vendaService.calcularTotalVendas());
        kpis.put("totalEntradasContabil", contabilService.calcularTotalEntradas());
        kpis.put("totalSaidasContabil", contabilService.calcularTotalSaidas());
        kpis.put("saldoContabil", contabilService.calcularSaldoAtual());
        kpis.put("totalTestDrives", testes.size());
        kpis.put("totalUsuarios", usuarios.size());
        kpis.put("totalLogsRegistrados", logs.size());

        visao.put("perfil", "PROPRIETARIO_ADM");
        visao.put("modo", "APENAS_LEITURA");
        visao.put("indicadores", kpis);
        visao.put("carros", carros);
        visao.put("vendas", vendas);
        visao.put("contabil", lancamentos);
        visao.put("testesDrive", testes);
        visao.put("usuarios", usuarios);
        visao.put("logsAuditoria", logs);

        return ResponseEntity.ok(visao);
    }

    @GetMapping("/carros")
    public ResponseEntity<List<Carro>> listarCarros() {
        return ResponseEntity.ok(carroService.findAll());
    }

    @GetMapping("/vendas")
    public ResponseEntity<List<Venda>> listarVendas() {
        return ResponseEntity.ok(vendaService.findAll());
    }

    @GetMapping("/contabil")
    public ResponseEntity<List<Contabil>> listarContabil() {
        return ResponseEntity.ok(contabilService.findAll());
    }

    @GetMapping("/teste-drives")
    public ResponseEntity<List<TesteDrive>> listarTestDrives() {
        return ResponseEntity.ok(testeDriveService.findAll());
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/logs")
    public ResponseEntity<List<LogOperacao>> listarLogs() {
        return ResponseEntity.ok(logOperacaoRepository.findAllByOrderByDataHoraDesc());
    }
}
