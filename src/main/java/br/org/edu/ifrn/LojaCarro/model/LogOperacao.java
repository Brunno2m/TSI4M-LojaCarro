package br.org.edu.ifrn.LojaCarro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_operacao")
public class LogOperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entidade", length = 40, nullable = false)
    private String entidade = "CARRO";

    @Column(name = "acao", nullable = false, length = 50)
    private String tipoOperacao;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "usuario_nome", length = 120)
    private String usuarioNome;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "detalhes", columnDefinition = "TEXT", nullable = false)
    private String detalhes;

    // Compatibilidade temporária com o consumidor e os DTOs antigos.
    // Estes campos não são colunas da nova tabela de auditoria.
    @Transient private Long carroId;
    @Transient private String carroModelo;
    @Transient private Integer carroAno;
    @Transient private Double carroPreco;
    @Transient private Double valor;
    @Transient private String usuarioCargo;
    @Transient private Double percentualComissao;
    @Transient private Double valorComissao;

    public LogOperacao() {
        this.dataHora = LocalDateTime.now();
    }

    public LogOperacao(String tipoOperacao, Long carroId, String carroModelo,
                       Integer carroAno, Double carroPreco, Long usuarioId,
                       String usuarioNome, String usuarioCargo,
                       LocalDateTime dataHora, String detalhes) {
        this.entidade = "CARRO";
        this.tipoOperacao = tipoOperacao;
        this.referenciaId = carroId;
        this.carroId = carroId;
        this.carroModelo = carroModelo;
        this.carroAno = carroAno;
        this.carroPreco = carroPreco;
        this.valor = carroPreco;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.usuarioCargo = usuarioCargo;
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
        this.detalhes = detalhes;
    }

    public LogOperacao(String entidade, String tipoOperacao, Long referenciaId,
                       Long usuarioId, String usuarioNome, String usuarioCargo,
                       Double valor, String detalhes) {
        this.entidade = entidade;
        this.tipoOperacao = tipoOperacao;
        this.referenciaId = referenciaId;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.usuarioCargo = usuarioCargo;
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
        this.detalhes = detalhes;
    }

    @PrePersist
    public void prepararPersistencia() {
        if (entidade == null) entidade = "CARRO";
        if (referenciaId == null) referenciaId = carroId;
        if (dataHora == null) dataHora = LocalDateTime.now();
        if (detalhes == null) detalhes = "Operação: " + tipoOperacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEntidade() { return entidade != null ? entidade : "CARRO"; }
    public void setEntidade(String entidade) { this.entidade = entidade; }
    public String getTipoOperacao() { return tipoOperacao; }
    public void setTipoOperacao(String tipoOperacao) { this.tipoOperacao = tipoOperacao; }
    public Long getReferenciaId() { return referenciaId != null ? referenciaId : carroId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public Long getCarroId() { return carroId; }
    public void setCarroId(Long carroId) {
        this.carroId = carroId;
        if (this.referenciaId == null) this.referenciaId = carroId;
    }
    public String getCarroModelo() { return carroModelo; }
    public void setCarroModelo(String carroModelo) { this.carroModelo = carroModelo; }
    public Integer getCarroAno() { return carroAno; }
    public void setCarroAno(Integer carroAno) { this.carroAno = carroAno; }
    public Double getCarroPreco() { return carroPreco; }
    public void setCarroPreco(Double carroPreco) {
        this.carroPreco = carroPreco;
        if (this.valor == null) this.valor = carroPreco;
    }
    public Double getValor() { return valor != null ? valor : carroPreco; }
    public void setValor(Double valor) { this.valor = valor; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
    public String getUsuarioCargo() { return usuarioCargo; }
    public void setUsuarioCargo(String usuarioCargo) { this.usuarioCargo = usuarioCargo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
    public Double getPercentualComissao() { return percentualComissao; }
    public void setPercentualComissao(Double percentualComissao) { this.percentualComissao = percentualComissao; }
    public Double getValorComissao() { return valorComissao; }
    public void setValorComissao(Double valorComissao) { this.valorComissao = valorComissao; }
}
