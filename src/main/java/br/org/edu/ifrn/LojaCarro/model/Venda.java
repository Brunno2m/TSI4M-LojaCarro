package br.org.edu.ifrn.LojaCarro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidade JPA para registro e controle de Vendas de veículos na concessionária.
 */
@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long carroId;

    @Column(nullable = false, length = 100)
    private String carroModelo;

    @Column(nullable = false, length = 100)
    private String clienteNome;

    @Column(length = 20)
    private String clienteCpf;

    @Column(length = 20)
    private String clienteTelefone;

    @Column(nullable = false)
    private Double valorVenda;

    @Column(nullable = false, length = 30)
    private String formaPagamento; // PIX, FINANCIAMENTO, A_VISTA, CARTAO, CONSORCIO

    private Long vendedorId;

    @Column(length = 100)
    private String vendedorNome;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime dataVenda;

    @Column(nullable = false, length = 20)
    private String status; // CONCLUIDA, CANCELADA

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public Venda() {
        this.dataVenda = LocalDateTime.now();
        this.status = "CONCLUIDA";
        this.formaPagamento = "PIX";
    }

    public Venda(Long carroId, String carroModelo, String clienteNome, String clienteCpf, String clienteTelefone,
                 Double valorVenda, String formaPagamento, Long vendedorId, String vendedorNome, String observacoes) {
        this.carroId = carroId;
        this.carroModelo = carroModelo;
        this.clienteNome = clienteNome;
        this.clienteCpf = clienteCpf;
        this.clienteTelefone = clienteTelefone;
        this.valorVenda = valorVenda;
        this.formaPagamento = formaPagamento;
        this.vendedorId = vendedorId;
        this.vendedorNome = vendedorNome;
        this.observacoes = observacoes;
        this.dataVenda = LocalDateTime.now();
        this.status = "CONCLUIDA";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCarroId() {
        return carroId;
    }

    public void setCarroId(Long carroId) {
        this.carroId = carroId;
    }

    public String getCarroModelo() {
        return carroModelo;
    }

    public void setCarroModelo(String carroModelo) {
        this.carroModelo = carroModelo;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteCpf() {
        return clienteCpf;
    }

    public void setClienteCpf(String clienteCpf) {
        this.clienteCpf = clienteCpf;
    }

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public void setClienteTelefone(String clienteTelefone) {
        this.clienteTelefone = clienteTelefone;
    }

    public Double getValorVenda() {
        return valorVenda;
    }

    public void setValorVenda(Double valorVenda) {
        this.valorVenda = valorVenda;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Long getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(Long vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getVendedorNome() {
        return vendedorNome;
    }

    public void setVendedorNome(String vendedorNome) {
        this.vendedorNome = vendedorNome;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
