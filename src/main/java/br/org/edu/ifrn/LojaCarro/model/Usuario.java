
package br.org.edu.ifrn.LojaCarro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    // Temporário: atributo Java cargo corresponde ao perfil
    @Column(name = "perfil", nullable = false, length = 20)
    private String cargo;

    // Temporário: atributo Java login corresponde ao email
    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String login;

    // Este campo deverá guardar somente o hash BCrypt
    @JsonIgnore
    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senha;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    // Preenchido automaticamente pelo banco
    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;



    public Usuario() {
    }

    public Usuario(Long id, String nome, String cargo,
                   String login, String senha) {
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
        this.login = login;
        this.senha = senha;
    }

    public Usuario(String nome, String cargo,
                   String login, String senha) {
        this.nome = nome;
        this.cargo = cargo;
        this.login = login;
        this.senha = senha;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
