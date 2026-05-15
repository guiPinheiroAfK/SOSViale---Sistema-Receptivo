package br.com.sosviale.offline.dto;

public class OfflineVeiculoDto {

    private Integer id;
    private String label;
    private String placa;
    private Integer capacidade;
    private String marca;
    private String tipo;

    public OfflineVeiculoDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
