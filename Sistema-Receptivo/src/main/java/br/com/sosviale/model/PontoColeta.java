package br.com.sosviale.model;

import jakarta.persistence.*;
import java.time.LocalTime;

// ponto de parada geografico previsto pra transfer (coordenadas podem ficar null)

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

    @Column(name = "latitude", columnDefinition = "numeric")
    private Double latitude;

    @Column(name = "longitude", columnDefinition = "numeric")
    private Double longitude;

    public PontoColeta() {
    }

    // ordem ignorada ate alguém plugar ordenacao aqui (?)
    public PontoColeta(String localColeta, Integer ordemParada, LocalTime horarioPrevisto) {
        this.localColeta = localColeta;
        this.horarioPrevisto = horarioPrevisto;
    }

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