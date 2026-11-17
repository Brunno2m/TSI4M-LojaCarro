
package br.org.edu.ifrn.LojaCarro.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
public class TesteDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Cliente responsável pelo agendamento
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    // Veículo escolhido
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carro_id", nullable = false)
    @JsonIgnore
    private Carro carro;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "inicio", nullable = false)
    private LocalDateTime dataHora;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "fim", nullable = false)
    private LocalDateTime fim;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "CONFIRMADO";

    @Column(
            name = "criado_em",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime criadoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;


    // Não serão persistidos no novo banco
    @Transient
    private Long usuarioId;

    @Transient
    private Long carroId;

    @Transient
    private String clienteNome;

    @Transient
    private String clienteTelefone;

    @Transient
    private String clienteCpf;

    @Transient
    private String clienteCnh;

    @Transient
    private String carroModelo;

    @Transient
    private String vendedorResponsavel;

    @Transient
    private String observacoes;



    public TesteDrive() {
    }

    // Construtor mantido para compatibilidade
    public TesteDrive(
            String clienteNome,
            String clienteTelefone,
            String clienteCpf,
            String clienteCnh,
            Long carroId,
            String carroModelo,
            LocalDateTime dataHora,
            String vendedorResponsavel,
            String observacoes
    ) {
        this.clienteNome = clienteNome;
        this.clienteTelefone = clienteTelefone;
        this.clienteCpf = clienteCpf;
        this.clienteCnh = clienteCnh;
        this.carroId = carroId;
        this.carroModelo = carroModelo;
        this.vendedorResponsavel = vendedorResponsavel;
        this.observacoes = observacoes;
        this.status = "CONFIRMADO";

        setDataHora(
                dataHora != null
                        ? dataHora
                        : LocalDateTime.now().plusDays(1)
        );
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        this.usuarioId =
                usuario != null ? usuario.getId() : null;
    }

    @JsonIgnore
    public Carro getCarro() {
        return carro;
    }

    public void setCarro(Carro carro) {
        this.carro = carro;
        this.carroId =
                carro != null ? carro.getId() : null;
    }

    public Long getUsuarioId() {
        return usuario != null
                ? usuario.getId()
                : usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getCarroId() {
        return carro != null
                ? carro.getId()
                : carroId;
    }

    public void setCarroId(Long carroId) {
        this.carroId = carroId;
    }

    public String getClienteNome() {
        return usuario != null
                ? usuario.getNome()
                : clienteNome;
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

    public String getCarroModelo() {
        return carro != null
                ? carro.getModelo()
                : carroModelo;
    }

    public void setCarroModelo(String carroModelo) {
        this.carroModelo = carroModelo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;

        this.fim = dataHora == null
                ? null
                : dataHora.plusMinutes(30);
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }

    public void setCanceladoEm(LocalDateTime canceladoEm) {
        this.canceladoEm = canceladoEm;
    }

    public String getVendedorResponsavel() {
        return vendedorResponsavel;
    }

    public void setVendedorResponsavel(
            String vendedorResponsavel
    ) {
        this.vendedorResponsavel = vendedorResponsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
