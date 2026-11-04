package br.org.edu.ifrn.LojaCarro;

import br.org.edu.ifrn.LojaCarro.model.*;
import br.org.edu.ifrn.LojaCarro.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContabilRepository contabilRepository;

    @Autowired
    private TesteDriveRepository testeDriveRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Override
    public void run(String... args) {
        // 1. Inicializa Usuários padrão se não existirem
        cadastrarSeNaoExistir("Carlos Proprietário", "PROPRIETARIO", "proprietario", "12345");
        cadastrarSeNaoExistir("Administrador Geral", "SUPERUSUARIO", "admin", "12345");
        cadastrarSeNaoExistir("Carlos Gerente", "GERENTE", "gerente", "12345");
        cadastrarSeNaoExistir("Ana Vendedora", "VENDEDOR", "vendedor", "12345");
        cadastrarSeNaoExistir("Roberto Contador", "CONTABIL", "contador", "12345");

        // 2. Inicializa estoque de carros se estiver vazio
        if (carroRepository.count() == 0) {
            carroRepository.save(new Carro("Gol", 2022, 45000.0));
            carroRepository.save(new Carro("Polo", 2023, 78000.0));
            carroRepository.save(new Carro("Ka", 2021, 42000.0));
            carroRepository.save(new Carro("Uno", 2020, 35000.0));
            carroRepository.save(new Carro("Fit", 2022, 68000.0));
        }

        // 3. Inicializa Lançamentos Contábeis de exemplo
        if (contabilRepository.count() == 0) {
            contabilRepository.save(new Contabil("Aporte Inicial de Capital dos Sócios", "ENTRADA", 200000.0, "INVESTIMENTO", "Carlos Proprietário", "Saldo inicial de abertura da loja"));
            contabilRepository.save(new Contabil("Pagamento de Aluguel do Showroom", "SAIDA", 5000.0, "DESPESA_FIXA", "Roberto Contador", "Mês corrente"));
            contabilRepository.save(new Contabil("Seguro e Manutenção do Pátio", "SAIDA", 2300.0, "MANUTENCAO", "Roberto Contador", "Revisão geral"));
            contabilRepository.save(new Contabil("Recebimento Venda Veículo: Gol (2022) (Cliente: Mariana Souza)", "ENTRADA", 45000.0, "VENDA", "Ana Vendedora", "Venda ID: 1, Pagamento: PIX"));
            contabilRepository.save(new Contabil("Comissão sobre Venda (3.0%): Gol (2022) - Ana Vendedora", "SAIDA", 1350.0, "COMISSAO", "Ana Vendedora", "Comissão de 3.0% referente à Venda ID: 1 (Cargo: VENDEDOR)"));
        }

        // 4. Inicializa Test Drives de exemplo
        if (testeDriveRepository.count() == 0) {
            testeDriveRepository.save(new TesteDrive(
                    "Marcos Vinicius",
                    "(84) 99887-1122",
                    "111.222.333-44",
                    "09876543210",
                    2L,
                    "Polo (2023)",
                    LocalDateTime.now().plusDays(2).withHour(14).withMinute(0),
                    "Ana Vendedora",
                    "Cliente tem interesse em financiamento"
            ));
        }

        // 5. Inicializa Vendas de exemplo
        if (vendaRepository.count() == 0) {
            vendaRepository.save(new Venda(
                    1L,
                    "Gol (2022)",
                    "Mariana Souza",
                    "555.666.777-88",
                    "(84) 98765-4321",
                    45000.0,
                    "PIX",
                    4L,
                    "Ana Vendedora",
                    "Venda à vista entregue no mesmo dia"
            ));
        }
    }

    private void cadastrarSeNaoExistir(String nome, String cargo, String login, String senha) {
        if (usuarioRepository.findByLogin(login).isEmpty()) {
            usuarioRepository.save(new Usuario(nome, cargo, login, senha));
        }
    }
}
