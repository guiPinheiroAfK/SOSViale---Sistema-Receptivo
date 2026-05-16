package br.com.sosviale.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @Column(name = "criado_em")
    @Temporal(TemporalType.TIMESTAMP)
    private Date criadoEm;

    public User() {}

    @PrePersist
    public void prePersist() {
        this.criadoEm = new Date();
    }

    public User(String nome, String usuario, String senha, boolean isAdmin) {
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senha;
        this.isAdmin = isAdmin;
        this.criadoEm = new Date();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public Date getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Date criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() {
        return nome + " (" + (isAdmin ? "ADMIN" : "USER") + ")";
    }
}