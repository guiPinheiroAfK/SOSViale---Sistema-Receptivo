package br.com.sosviale.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "pontos_coleta")
public class PontoColeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "local_coleta", nullable = false, length = 100)
    private String localColeta;

    @Column(name = "horario_previsto")
    private LocalTime horarioPrevisto;

    // coordenadas para futura integração com pathfinding
    @Column(name = "latitude", columnDefinition = "numeric")
    private Double latitude;

    @Column(name = "longitude", columnDefinition = "numeric")
    private Double longitude;

    // construtor padrão obrigatório pelo JPA
    public PontoColeta() {
    }

    // construtor auxiliar sem coordenadas (preenchidas depois ou via geocodificação)
    public PontoColeta(String localColeta, Integer ordemParada, LocalTime horarioPrevisto) {
        this.localColeta = localColeta;
        this.horarioPrevisto = horarioPrevisto;
    }

    // getters e setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLocalColeta() { return localColeta; }
    public void setLocalColeta(String localColeta) { this.localColeta = localColeta; }

    public LocalTime getHorarioPrevisto() { return horarioPrevisto; }
    public void setHorarioPrevisto(LocalTime horarioPrevisto) { this.horarioPrevisto = horarioPrevisto; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public void setOrdemParada(int ordem) {
    }

    @Override
    public String toString() {
        return this.localColeta != null ? this.localColeta : "";
    }
}