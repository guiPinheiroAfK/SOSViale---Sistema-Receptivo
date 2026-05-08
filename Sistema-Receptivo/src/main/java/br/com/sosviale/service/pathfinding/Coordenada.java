package br.com.sosviale.service.pathfinding;

import br.com.sosviale.model.PontoColeta;

/**
 * Representa um nó no grafo de rota: um ponto geográfico com nome legível
 * e referência opcional ao PontoColeta do banco.
 *
 * Imutável por design — coordenadas não mudam após a criação.
 */
public final class Coordenada {

    private final double     latitude;
    private final double     longitude;
    private final String     nome;

    /** Referência ao PontoColeta persistido. Null quando o nó representa
     *  a posição atual do motorista (ponto de partida virtual). */
    private final PontoColeta pontoColeta;

    public Coordenada(double latitude, double longitude, String nome, PontoColeta pontoColeta) {
        this.latitude   = latitude;
        this.longitude  = longitude;
        this.nome       = nome;
        this.pontoColeta = pontoColeta;
    }

    /** Construtor para pontos sem referência ao banco (ex: posição do motorista). */
    public Coordenada(double latitude, double longitude, String nome) {
        this(latitude, longitude, nome, null);
    }

    public double     getLatitude()   { return latitude;   }
    public double     getLongitude()  { return longitude;  }
    public String     getNome()       { return nome;       }
    public PontoColeta getPontoColeta() { return pontoColeta; }

    /** True se este nó representa a posição atual do motorista (ponto virtual). */
    public boolean isPontoDePartida() { return pontoColeta == null; }

    @Override
    public String toString() {
        return String.format("%s (%.5f, %.5f)", nome, latitude, longitude);
    }
}
