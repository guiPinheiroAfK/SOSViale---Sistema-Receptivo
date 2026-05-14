package br.com.sosviale.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paradas_os")
public class ParadaOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "os_id", nullable = false)
    private OrdemServico ordemServico;

    @Column(name = "ordem_parada", nullable = false)
    private Integer ordemParada;

    @Column(name = "local_parada", nullable = false)
    private String localParada;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "horario_previsto")
    private LocalTime horarioPrevisto;

    @Column(name = "acao")
    private String acao; // Ex: "EMBARQUE", "DESEMBARQUE"

    @Column(name = "status_parada")
    private String statusParada = "PENDENTE";

    // Sem CascadeType.ALL aqui para não excluir o Transfer inteiro sem querer!
    @ManyToMany
    @JoinTable(
            name = "parada_os_transfers",
            joinColumns = @JoinColumn(name = "parada_os_id"),
            inverseJoinColumns = @JoinColumn(name = "transfer_id")
    )
    private List<Transfer> transfers = new ArrayList<>();

    public ParadaOS() {}

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public OrdemServico getOrdemServico() { return ordemServico; }
    public void setOrdemServico(OrdemServico ordemServico) { this.ordemServico = ordemServico; }

    public Integer getOrdemParada() { return ordemParada; }
    public void setOrdemParada(Integer ordemParada) { this.ordemParada = ordemParada; }

    public String getLocalParada() { return localParada; }
    public void setLocalParada(String localParada) { this.localParada = localParada; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalTime getHorarioPrevisto() { return horarioPrevisto; }
    public void setHorarioPrevisto(LocalTime horarioPrevisto) { this.horarioPrevisto = horarioPrevisto; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getStatusParada() { return statusParada; }
    public void setStatusParada(String statusParada) { this.statusParada = statusParada; }

    public List<Transfer> getTransfers() { return transfers; }
    public void setTransfers(List<Transfer> transfers) { this.transfers = transfers; }
}