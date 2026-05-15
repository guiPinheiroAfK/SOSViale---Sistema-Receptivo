package br.com.sosviale.offline.dto;

import java.util.ArrayList;
import java.util.List;

public class OfflineOrdemServicoDto {

    private Integer id;
    private String dataServico;
    private String status;
    private OfflineMotoristaDto motorista;
    private OfflineVeiculoDto veiculo;
    private List<OfflineTransferDto> transfers = new ArrayList<>();

    public OfflineOrdemServicoDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDataServico() { return dataServico; }
    public void setDataServico(String dataServico) { this.dataServico = dataServico; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OfflineMotoristaDto getMotorista() { return motorista; }
    public void setMotorista(OfflineMotoristaDto motorista) { this.motorista = motorista; }

    public OfflineVeiculoDto getVeiculo() { return veiculo; }
    public void setVeiculo(OfflineVeiculoDto veiculo) { this.veiculo = veiculo; }

    public List<OfflineTransferDto> getTransfers() { return transfers; }
    public void setTransfers(List<OfflineTransferDto> transfers) {
        this.transfers = transfers != null ? transfers : new ArrayList<>();
    }
}
