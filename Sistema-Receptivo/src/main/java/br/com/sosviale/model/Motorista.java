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

    // CNH deve ser única no sistema
    @Column(nullable = false, unique = true, length = 20)
    private String cnh;

    @Column(name = "latitude_atual")
    private Double latitudeAtual;

    @Column(name = "longitude_atual")
    private Double longitudeAtual;

    // construtor padrão obrigatório pelo JPA
    public Motorista() {
    }

    // construtor auxiliar para facilitar a criação de objetos em testes e serviços
    public Motorista(String nome, String cnh) {
        this.nome = nome;
        this.cnh = cnh;
    }

    // getters e setters
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
}
