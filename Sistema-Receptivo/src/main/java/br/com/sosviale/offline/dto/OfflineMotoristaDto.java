package br.com.sosviale.offline.dto;

// bean jackson: motorista serializado no snapshot

public class OfflineMotoristaDto {

    private Integer id;
    private String nome;
    private String cnh;
    private String telefone;

    public OfflineMotoristaDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
}
