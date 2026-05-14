package br.com.sosviale.service.pathfinding;

import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;

public final class Coordenada {

    private final double latitude;
    private final double longitude;
    private final String nome;
    private final PontoColeta pontoColeta;
    private final Transfer transfer; // Novo campo

    // 1. CONSTRUTOR PRINCIPAL (5 parâmetros - O mais completo)
    public Coordenada(double latitude, double longitude, String nome, PontoColeta pontoColeta, Transfer transfer) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.nome = nome;
        this.pontoColeta = pontoColeta;
        this.transfer = transfer;
    }

    // 2. CONSTRUTOR DE COMPATIBILIDADE (4 parâmetros - Resolve o seu erro!)
    public Coordenada(double latitude, double longitude, String nome, PontoColeta pontoColeta) {
        this(latitude, longitude, nome, pontoColeta, null);
    }

    // 3. CONSTRUTOR PARA MOTORISTA/PONTO VIRTUAL (3 parâmetros)
    public Coordenada(double latitude, double longitude, String nome) {
        this(latitude, longitude, nome, null, null);
    }

    // Getters
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