package br.com.sosviale.offline.dto;

// passageiro achatado no json do transfer

public class OfflinePassageiroDto {

    private Integer id;
    private String nome;
    private String documento;
    private String tipoDocumento;
    private String nacionalidade;

    public OfflinePassageiroDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNacionalidade() { return nacionalidade; }
    public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }
}
