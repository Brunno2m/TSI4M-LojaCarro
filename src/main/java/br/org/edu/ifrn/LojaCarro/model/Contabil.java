package br.org.edu.ifrn.LojaCarro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidade JPA para Lançamentos Contábeis / Financeiros da concessionária.
 */
@Entity
@Table(name = "contabil")
public class Contabil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(nullable = false, length = 20)
    private String tipo; // ENTRADA (Receita) ou SAIDA (Despesa)

    @Column(nullable = false)
    private Double valor;

    @Column(length = 50)
    private String categoria; // VENDA, MANUTENCAO, COMISSAO, IMPOSTO, DESPESA_FIXA, OUTROS

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime dataLancamento;

    @Column(length = 100)
    private String responsavel;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public Contabil() {
        this.dataLancamento = LocalDateTime.now();
    }

    public Contabil(String descricao, String tipo, Double valor, String categoria, String responsavel, String observacoes) {
        this.descricao = descricao;
        this.tipo = tipo;
        this.valor = valor;
        this.categoria = categoria;
        this.responsavel = responsavel;
        this.observacoes = observacoes;
        this.dataLancamento = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public LocalDateTime getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDateTime dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
