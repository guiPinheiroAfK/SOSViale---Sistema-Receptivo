package br.com.sosviale.model;

import jakarta.persistence.*;

@Entity
@Table(name = "motoristas")
public class Motorista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    // cnh unica no sistema
    @Column(nullable = false, unique = true, length = 20)
    private String cnh;

    @Column(length = 20)
    private String telefone;

    @Column(name = "latitude_atual")
    private Double latitudeAtual;

    @Column(name = "longitude_atual")
    private Double longitudeAtual;

    public Motorista() {
    }

    // atalho pra teste / seed
    public Motorista(String nome, String cnh) {
        this.nome = nome;
        this.cnh = cnh;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }

    public Double getLatitudeAtual() { return latitudeAtual; }
    public void setLatitudeAtual(Double latitudeAtual) { this.latitudeAtual = latitudeAtual; }

    public Double getLongitudeAtual() { return longitudeAtual; }
    public void setLongitudeAtual(Double longitudeAtual) { this.longitudeAtual = longitudeAtual; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    @Override
    public String toString() {
        return id + " - " + nome;
    }
}
