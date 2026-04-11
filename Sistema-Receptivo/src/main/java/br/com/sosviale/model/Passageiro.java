package br.com.sosviale.model;

import jakarta.persistence.*;

@Entity
@Table(name = "passageiros")
public class Passageiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 20)
    private String documento;

    // nacionalidade padrão para passageiros sem informação explícita
    @Column(length = 50)
    private String nacionalidade = "Brasileira";

    // construtor padrão obrigatório pelo JPA
    public Passageiro() {
    }

    // construtor auxiliar para facilitar criação em testes e serviços
    public Passageiro(String nome, String documento, String nacionalidade) {
        this.nome = nome;
        this.documento = documento;
        this.nacionalidade = nacionalidade;
    }

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }
}