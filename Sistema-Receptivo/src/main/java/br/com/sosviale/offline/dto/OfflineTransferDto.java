package br.com.sosviale.offline.dto;

// transfer + pax list pra ida e volta json

import java.util.ArrayList;
import java.util.List;

public class OfflineTransferDto {

    private Integer id;
    private Integer osId;
    private String origem;
    private String destino;
    private String dataTransfer;
    private String horaTransfer;
    private String status;
    private List<OfflinePassageiroDto> passageiros = new ArrayList<>();

    public OfflineTransferDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOsId() { return osId; }
    public void setOsId(Integer osId) { this.osId = osId; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public String getDataTransfer() { return dataTransfer; }
    public void setDataTransfer(String dataTransfer) { this.dataTransfer = dataTransfer; }

    public String getHoraTransfer() { return horaTransfer; }
    public void setHoraTransfer(String horaTransfer) { this.horaTransfer = horaTransfer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OfflinePassageiroDto> getPassageiros() { return passageiros; }
    public void setPassageiros(List<OfflinePassageiroDto> passageiros) {
        this.passageiros = passageiros != null ? passageiros : new ArrayList<>();
    }
}
