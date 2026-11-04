package br.org.edu.ifrn.LojaCarro.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Objeto de Transferência de Dados (DTO) para envio de mensagens de auditoria/log
 * através do RabbitMQ para o módulo de log.
 */
public class LogOperacaoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String entidade; // CARRO, USUARIO, CONTABIL, TESTE_DRIVE, VENDAS
    private String tipoOperacao; // CADASTRO, ATUALIZACAO, EXCLUSAO
    private Long referenciaId;
    private Long carroId;
    private String carroModelo;
    private Integer carroAno;
    private Double carroPreco;
    private Double valor;
    private Long usuarioId;
    private String usuarioNome;
    private String usuarioCargo;
    private Double percentualComissao;
    private Double valorComissao;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataHora;

    private String detalhes;

    public LogOperacaoDTO() {
        this.dataHora = LocalDateTime.now();
        this.entidade = "CARRO";
    }

    // Construtor compatível com operações anteriores de Carro
    public LogOperacaoDTO(String tipoOperacao, Long carroId, String carroModelo, Integer carroAno, Double carroPreco,
                          Long usuarioId, String usuarioNome, String usuarioCargo, String detalhes) {
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
        this.dataHora = LocalDateTime.now();
        this.detalhes = detalhes;
    }

    // Construtor genérico para todas as entidades (Contábil, Teste Drive, Usuário, Vendas)
    public LogOperacaoDTO(String entidade, String tipoOperacao, Long referenciaId,
                          Double valor, Long usuarioId, String usuarioNome, String usuarioCargo, String detalhes) {
        this.entidade = entidade;
        this.tipoOperacao = tipoOperacao;
        this.referenciaId = referenciaId;
        this.valor = valor;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.usuarioCargo = usuarioCargo;
        this.dataHora = LocalDateTime.now();
        this.detalhes = detalhes;
    }

    public String getEntidade() {
        return entidade != null ? entidade : "CARRO";
    }

    public void setEntidade(String entidade) {
        this.entidade = entidade;
    }

    public String getTipoOperacao() {
        return tipoOperacao;
    }

    public void setTipoOperacao(String tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }

    public Long getReferenciaId() {
        return referenciaId != null ? referenciaId : carroId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public Long getCarroId() {
        return carroId;
    }

    public void setCarroId(Long carroId) {
        this.carroId = carroId;
        if (this.referenciaId == null) {
            this.referenciaId = carroId;
        }
    }

    public String getCarroModelo() {
        return carroModelo;
    }

    public void setCarroModelo(String carroModelo) {
        this.carroModelo = carroModelo;
    }

    public Integer getCarroAno() {
        return carroAno;
    }

    public void setCarroAno(Integer carroAno) {
        this.carroAno = carroAno;
    }

    public Double getCarroPreco() {
        return carroPreco;
    }

    public void setCarroPreco(Double carroPreco) {
        this.carroPreco = carroPreco;
        if (this.valor == null) {
            this.valor = carroPreco;
        }
    }

    public Double getValor() {
        return valor != null ? valor : carroPreco;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioCargo() {
        return usuarioCargo;
    }

    public void setUsuarioCargo(String usuarioCargo) {
        this.usuarioCargo = usuarioCargo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public Double getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(Double percentualComissao) {
        this.percentualComissao = percentualComissao;
    }

    public Double getValorComissao() {
        return valorComissao;
    }

    public void setValorComissao(Double valorComissao) {
        this.valorComissao = valorComissao;
    }

    @Override
    public String toString() {
        return "LogOperacaoDTO{" +
                "entidade='" + getEntidade() + '\'' +
                ", tipoOperacao='" + tipoOperacao + '\'' +
                ", referenciaId=" + getReferenciaId() +
                ", carroModelo='" + carroModelo + '\'' +
                ", usuarioNome='" + usuarioNome + '\'' +
                ", valor=" + getValor() +
                ", dataHora=" + dataHora +
                ", detalhes='" + detalhes + '\'' +
                '}';
    }
}
