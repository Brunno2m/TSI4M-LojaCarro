package br.org.edu.ifrn.LojaCarro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidade JPA para Agendamentos e Registros de Test Drive na concessionária.
 */
@Entity
@Table(name = "teste_drive")
public class TesteDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String clienteNome;

    @Column(nullable = false, length = 20)
    private String clienteTelefone;

    @Column(length = 20)
    private String clienteCpf;

    @Column(length = 20)
    private String clienteCnh;

    private Long carroId;

    @Column(nullable = false, length = 100)
    private String carroModelo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 20)
    private String status; // AGENDADO, REALIZADO, CANCELADO

    @Column(length = 100)
    private String vendedorResponsavel;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    public TesteDrive() {
        this.status = "AGENDADO";
        this.dataHora = LocalDateTime.now().plusDays(1);
    }

    public TesteDrive(String clienteNome, String clienteTelefone, String clienteCpf, String clienteCnh,
                      Long carroId, String carroModelo, LocalDateTime dataHora, String vendedorResponsavel, String observacoes) {
        this.clienteNome = clienteNome;
        this.clienteTelefone = clienteTelefone;
        this.clienteCpf = clienteCpf;
        this.clienteCnh = clienteCnh;
        this.carroId = carroId;
        this.carroModelo = carroModelo;
        this.dataHora = (dataHora != null) ? dataHora : LocalDateTime.now().plusDays(1);
        this.status = "AGENDADO";
        this.vendedorResponsavel = vendedorResponsavel;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public void setClienteTelefone(String clienteTelefone) {
        this.clienteTelefone = clienteTelefone;
    }

    public String getClienteCpf() {
        return clienteCpf;
    }

    public void setClienteCpf(String clienteCpf) {
        this.clienteCpf = clienteCpf;
    }

    public String getClienteCnh() {
        return clienteCnh;
    }

    public void setClienteCnh(String clienteCnh) {
        this.clienteCnh = clienteCnh;
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVendedorResponsavel() {
        return vendedorResponsavel;
    }

    public void setVendedorResponsavel(String vendedorResponsavel) {
        this.vendedorResponsavel = vendedorResponsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
