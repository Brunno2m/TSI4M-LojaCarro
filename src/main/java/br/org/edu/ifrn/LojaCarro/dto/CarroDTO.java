package br.org.edu.ifrn.LojaCarro.dto;

public class CarroDTO {
    private String modelo;
    private String marca;
    private Integer ano;
    private Double preco;

    public CarroDTO() {}

    // Getters e Setters
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }
}