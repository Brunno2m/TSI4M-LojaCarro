
package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class CarroService {

    @Autowired
    public CarroRepository carroRepository;

    private static final Set<String> SITUACOES = Set.of(
            "DISPONIVEL",
            "VENDIDO",
            "INATIVO"
    );

    // Cadastrar veículo
    @Transactional
    public Carro save(Carro c) {

        if (c == null) {
            erro("Informe os dados do veículo.");
        }

        c.setId(null);

        if (c.getSituacao() == null) {
            c.setSituacao("DISPONIVEL");
        }

        validarCarro(c);

        return carroRepository.save(c);
    }

    // Atualizar veículo
    @Transactional
    public Carro update(Carro c) {

        if (c == null) {
            erro("Informe os dados do veículo.");
        }

        Carro carro = buscarObrigatorio(c.getId());

        carro.setMarca(c.getMarca());
        carro.setModelo(c.getModelo());
        carro.setAno(c.getAno());
        carro.setAnoModelo(c.getAnoModelo());
        carro.setCor(c.getCor());
        carro.setQuilometragem(c.getQuilometragem());
        carro.setTipoCombustivel(c.getTipoCombustivel());
        carro.setTipoTransmissao(c.getTipoTransmissao());
        carro.setPreco(c.getPreco());
        carro.setDescricao(c.getDescricao());
        carro.setSituacao(c.getSituacao());

        // Preserva a fotografia anterior
        // caso uma nova não seja informada
        if (c.getFotoPrincipal() != null
                && !c.getFotoPrincipal().isBlank()) {

            carro.setFotoPrincipal(c.getFotoPrincipal());
        }

        validarCarro(carro);

        return carroRepository.save(carro);
    }

    // Exclusão lógica
    @Transactional
    public void deleteById(Long id) {

        Carro carro = buscarObrigatorio(id);

        carro.setSituacao("INATIVO");

        carroRepository.save(carro);
    }

    // Marcar veículo como vendido
    @Transactional
    public Carro marcarComoVendido(Long id) {

        Carro carro = buscarObrigatorio(id);

        carro.setSituacao("VENDIDO");

        return carroRepository.save(carro);
    }

    // Pesquisar veículo por ID
    public Optional<Carro> findById(Long id) {

        validarId(id);

        return carroRepository.findById(id);
    }

    // Listar todos os veículos
    public List<Carro> findAll() {
        return carroRepository.findAll();
    }

    // Listar somente veículos disponíveis
    public List<Carro> listarDisponiveis() {

        return carroRepository.findAll()
                .stream()
                .filter(c -> "DISPONIVEL".equals(
                        c.getSituacao()))
                .toList();
    }

    // Pesquisar veículo por modelo
    public Optional<Carro> findByModelo(String modelo) {

        validarTexto(modelo, "Modelo", 100);

        List<Carro> encontrados = carroRepository.findAll()
                .stream()
                .filter(c -> modelo.trim()
                        .equalsIgnoreCase(c.getModelo()))
                .toList();

        if (encontrados.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Existe mais de um veículo com esse modelo. Utilize o ID."
            );
        }

        return encontrados.stream().findFirst();
    }

    // Compatibilidade com o projeto anterior
    public Carro saveFromLegacy(String modelo, double preco) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Utilize o cadastro completo de veículos."
        );
    }

    // Atualizar preço por modelo
    @Transactional
    public Carro updateByModelo(String modelo, double preco) {

        Carro carro = localizarCarroPorModelo(modelo);

        validarPreco(preco);

        carro.setPreco(preco);

        return carroRepository.save(carro);
    }

    // Desativar veículo por modelo
    @Transactional
    public Carro deleteByModelo(String modelo) {

        Carro carro = localizarCarroPorModelo(modelo);

        carro.setSituacao("INATIVO");

        return carroRepository.save(carro);
    }

    // Pesquisa e filtros do catálogo
    public List<Carro> filtrar(
            String marca,
            String modelo,
            Double precoMinimo,
            Double precoMaximo,
            Integer anoModelo
    ) {

        if (precoMinimo != null
                && (!Double.isFinite(precoMinimo)
                || precoMinimo < 0)) {

            erro("Preço mínimo inválido.");
        }

        if (precoMaximo != null
                && (!Double.isFinite(precoMaximo)
                || precoMaximo < 0)) {

            erro("Preço máximo inválido.");
        }

        if (precoMinimo != null
                && precoMaximo != null
                && precoMinimo > precoMaximo) {

            erro("Faixa de preço inválida.");
        }

        return listarDisponiveis()
                .stream()
                .filter(c -> marca == null
                        || contem(c.getMarca(), marca))
                .filter(c -> modelo == null
                        || contem(c.getModelo(), modelo))
                .filter(c -> precoMinimo == null
                        || c.getPreco() >= precoMinimo)
                .filter(c -> precoMaximo == null
                        || c.getPreco() <= precoMaximo)
                .filter(c -> anoModelo == null
                        || anoModelo.equals(c.getAnoModelo()))
                .toList();
    }

    // Localizar veículo pelo ID
    private Carro buscarObrigatorio(Long id) {

        validarId(id);

        return carroRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veículo não encontrado."
                        ));
    }

    // Localizar veículo pelo modelo
    private Carro localizarCarroPorModelo(String modelo) {

        return findByModelo(modelo)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Veículo não encontrado."
                        ));
    }

    // Validação completa do veículo
    private void validarCarro(Carro carro) {

        validarTexto(carro.getMarca(), "Marca", 60);

        validarTexto(carro.getModelo(), "Modelo", 100);

        validarTexto(carro.getCor(), "Cor", 40);

        validarTexto(
                carro.getTipoCombustivel(),
                "Combustível",
                30
        );

        validarTexto(
                carro.getTipoTransmissao(),
                "Transmissão",
                30
        );

        validarTexto(
                carro.getDescricao(),
                "Descrição",
                2000
        );

        validarTexto(
                carro.getFotoPrincipal(),
                "Fotografia",
                255
        );

        if (carro.getAno() < 1886
                || carro.getAno() > 2100) {

            erro("Ano de fabricação inválido.");
        }

        if (carro.getAnoModelo() == null
                || carro.getAnoModelo() < 1886
                || carro.getAnoModelo() > 2101) {

            erro("Ano do modelo inválido.");
        }

        if (carro.getQuilometragem() == null
                || carro.getQuilometragem() < 0) {

            erro("A quilometragem não pode ser negativa.");
        }

        validarPreco(carro.getPreco());

        if (carro.getSituacao() == null
                || !SITUACOES.contains(carro.getSituacao())) {

            erro("Situação do veículo inválida.");
        }
    }

    // Validação do preço
    private void validarPreco(double preco) {

        if (!Double.isFinite(preco)
                || preco <= 0
                || preco > 9999999999.99) {

            erro("O preço do veículo deve ser positivo e válido.");
        }

        if (BigDecimal.valueOf(preco)
                .stripTrailingZeros().scale() > 2) {

            erro("O preço deve possuir até duas casas decimais.");
        }
    }

    // Validação dos campos de texto
    private void validarTexto(
            String valor,
            String campo,
            int limite
    ) {

        if (valor == null || valor.isBlank()) {

            erro(campo + " é obrigatório.");
        }

        if (valor.length() > limite) {

            erro(campo + " deve possuir no máximo "
                    + limite + " caracteres.");
        }
    }

    // Validação do identificador
    private void validarId(Long id) {

        if (id == null || id <= 0) {

            erro("O ID do veículo deve ser positivo.");
        }
    }

    // Pesquisa sem distinção de maiúsculas e minúsculas
    private boolean contem(String valor, String pesquisa) {

        return valor != null
                && valor.toLowerCase(Locale.ROOT)
                .contains(pesquisa.trim()
                        .toLowerCase(Locale.ROOT));
    }

    // Tratamento de erros de validação
    private void erro(String mensagem) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                mensagem
        );
    }
}
