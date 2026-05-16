package br.com.sosviale.service.pathfinding;

import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;

public class Coordenada {

    private final double latitude;
    private final double longitude;
    private final String nome;
    private final PontoColeta pontoColeta;
    private final Transfer transfer; // Novo campo

    // construtor principal
    public Coordenada(double latitude, double longitude, String nome, PontoColeta pontoColeta, Transfer transfer) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.nome = nome;
        this.pontoColeta = pontoColeta;
        this.transfer = transfer;
    }

    // construtor de compatibilidade
    public Coordenada(double latitude, double longitude, String nome, PontoColeta pontoColeta) {
        this(latitude, longitude, nome, pontoColeta, null);
    }

    // construtor para motorista
    public Coordenada(double latitude, double longitude, String nome) {
        this(latitude, longitude, nome, null, null);
    }

    // getters
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getNome() { return nome; }
    public PontoColeta getPontoColeta() { return pontoColeta; }
    public Transfer getTransfer() { return transfer; }

    public boolean isPontoDePartida() { return pontoColeta == null && transfer == null; }

    @Override
    public String toString() {
        return String.format("%s (%.5f, %.5f)", nome, latitude, longitude);
    }
}