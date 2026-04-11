package br.com.sosviale.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "pontos_coleta")
public class PontoColeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;

    @Column(name = "local_coleta", nullable = false, length = 100)
    private String localColeta;

    // define a sequência de paradas dentro do transfer
    @Column(name = "ordem_parada", nullable = false)
    private Integer ordemParada;

    @Column(name = "horario_previsto")
    private LocalTime horarioPrevisto;

    // coordenadas para futura integração com pathfinding
    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    // construtor padrão obrigatório pelo JPA
    public PontoColeta() {
    }

    // construtor auxiliar sem coordenadas (preenchidas depois ou via geocodificação)
    public PontoColeta(Transfer transfer, String localColeta, Integer ordemParada, LocalTime horarioPrevisto) {
        this.transfer = transfer;
        this.localColeta = localColeta;
        this.ordemParada = ordemParada;
        this.horarioPrevisto = horarioPrevisto;
    }

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Transfer getTransfer() { return transfer; }
    public void setTransfer(Transfer transfer) { this.transfer = transfer; }

    public String getLocalColeta() { return localColeta; }
    public void setLocalColeta(String localColeta) { this.localColeta = localColeta; }

    public Integer getOrdemParada() { return ordemParada; }
    public void setOrdemParada(Integer ordemParada) { this.ordemParada = ordemParada; }

    public LocalTime getHorarioPrevisto() { return horarioPrevisto; }
    public void setHorarioPrevisto(LocalTime horarioPrevisto) { this.horarioPrevisto = horarioPrevisto; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}