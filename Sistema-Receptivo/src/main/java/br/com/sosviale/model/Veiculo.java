package br.com.sosviale.model;

import jakarta.persistence.*;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String label;

    // placa deve ser única no sistema
    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false)
    private Integer capacidade;

    // construtor padrão obrigatório pelo JPA
    public Veiculo() {
    }

    // construtor completo para facilitar a criação em testes e serviços
    public Veiculo(String label, String placa, Integer capacidade) {
        this.label = label;
        this.placa = placa;
        this.capacidade = capacidade;
    }

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
}
