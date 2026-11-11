
package br.org.edu.ifrn.LojaCarro.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "carro")
public class Carro {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "marca", nullable = false, length = 60)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 100)
    private String modelo;

    // Ano de fabricação
    @Column(name = "ano", nullable = false)
    private int ano;

    @Column(name = "ano_modelo", nullable = false)
    private Integer anoModelo;

    @Column(name = "cor", nullable = false, length = 40)
    private String cor;

    @Column(name = "quilometragem", nullable = false)
    private Long quilometragem;

    @Column(name = "tipo_combustivel", nullable = false, length = 30)
    private String tipoCombustivel;

    @Column(name = "tipo_transmissao", nullable = false, length = 30)
    private String tipoTransmissao;

    @Column(
            name = "preco",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal preco;

    @Column(
            name = "descricao",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String descricao;

    @Column(name = "situacao", nullable = false, length = 20)
    private String situacao = "DISPONIVEL";

    // Caminho da fotografia armazenada no servidor
    @Column(name = "foto_principal", nullable = false, length = 255)
    private String fotoPrincipal;



    public Carro() {
    }

    // Mantido para compatibilidade com o projeto anterior
    public Carro(String modelo, int ano) {
        this.modelo = modelo;
        this.ano = ano;
        this.preco = BigDecimal.ZERO;
    }

    // Mantido para compatibilidade com o projeto anterior
    public Carro(String modelo, int ano, double preco) {
        this.modelo = modelo;
        this.ano = ano;
        this.preco = BigDecimal.valueOf(preco);
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public Integer getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Long getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(Long quilometragem) {
        this.quilometragem = quilometragem;
    }

    public String getTipoCombustivel() {
        return tipoCombustivel;
    }

    public void setTipoCombustivel(String tipoCombustivel) {
        this.tipoCombustivel = tipoCombustivel;
    }

    public String getTipoTransmissao() {
        return tipoTransmissao;
    }

    public void setTipoTransmissao(String tipoTransmissao) {
        this.tipoTransmissao = tipoTransmissao;
    }

    public double getPreco() {
        return preco == null ? 0.0 : preco.doubleValue();
    }

    public void setPreco(double preco) {
        this.preco = BigDecimal.valueOf(preco);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getFotoPrincipal() {
        return fotoPrincipal;
    }

    public void setFotoPrincipal(String fotoPrincipal) {
        this.fotoPrincipal = fotoPrincipal;
    }
}
