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

    // Aqui está o segredo: dizemos ao JPA para cadastrar o NOME do enum (ex: "CPF")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(length = 50)
    private String nacionalidade = "Brasileira";

    public Passageiro() {
    }

    // Atualizamos o construtor para exigir o tipo do documento agora
    public Passageiro(String nome, String documento, TipoDocumento tipoDocumento, String nacionalidade) {
        this.nome = nome;
        this.documento = documento;
        this.tipoDocumento = tipoDocumento;
        this.nacionalidade = (nacionalidade == null || nacionalidade.trim().isEmpty()) ? "Brasileira" : nacionalidade;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }
}