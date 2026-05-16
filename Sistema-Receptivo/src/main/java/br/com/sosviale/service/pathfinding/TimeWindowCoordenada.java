package br.com.sosviale.service.pathfinding;

import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;

import java.time.LocalTime;

// extensão de Coordenada que inclui janela de tempo (time window)

 /* Problema real:
    Se Transfer B é às 08:30 e Transfer A é às 08:45, o algoritmo
    NÃO pode inverter a ordem, mesmo que geograficamente fique mais próximo
    Porque o passageiro de B espera ser buscado às 08:30, não depois de A

   A solucao é cada coordenada carrega sua hora prometida. O PathFinding respeita isso
 */
public final class TimeWindowCoordenada extends Coordenada {

    private final LocalTime horarioPrevisto;    // Hora que o passageiro espera ser buscado
    private final LocalTime horaLimite;         // Margem de tolerância (horarioPrevisto + 15 min)
    private final boolean ehPontoDePartida;


    // construtores
    public TimeWindowCoordenada(double latitude, double longitude, String nome,
                                PontoColeta pontoColeta, Transfer transfer,
                                LocalTime horarioPrevisto, int marginemMinutos) {
        super(latitude, longitude, nome, pontoColeta, transfer);
        this.horarioPrevisto = horarioPrevisto;
        this.horaLimite = horarioPrevisto != null
                ? horarioPrevisto.plusMinutes(marginemMinutos)
                : null;
        this.ehPontoDePartida = false;
    }

    // construtor para ponto de partida (motorista)
    public TimeWindowCoordenada(double latitude, double longitude, String nome) {
        super(latitude, longitude, nome);
        this.horarioPrevisto = null;
        this.horaLimite = null;
        this.ehPontoDePartida = true;
    }

    public TimeWindowCoordenada(double latitude, double longitude, String nome,
                                PontoColeta pontoColeta, LocalTime horarioPrevisto) {
        super(latitude, longitude, nome, pontoColeta);
        this.horarioPrevisto = horarioPrevisto;
        this.horaLimite = horarioPrevisto != null
                ? horarioPrevisto.plusMinutes(15)
                : null;
        this.ehPontoDePartida = false;
    }

    // getters
    public LocalTime getHorarioPrevisto() { return horarioPrevisto; }
    public LocalTime getHoraLimite()      { return horaLimite; }
    public boolean isPontoDePartida()     { return ehPontoDePartida; }

    // verifica se a chegada em um horário é viável para este ponto.
    // regra: horario <= horaLimite (com margem de 15 min)

    public boolean isChegadaViavel(LocalTime horaChegada) {
        if (horaLimite == null) return true; // Ponto de partida ou sem restrição
        return !horaChegada.isAfter(horaLimite);
    }

    // verifica se uma chegada está "muito cedo" (mais de 30 min antes do horário).
    //O motorista pode chegar cedo, mas esperar > 30 min é ineficiente.

    public boolean isChegadaMuitoCedo(LocalTime horaChegada) {
        if (horarioPrevisto == null) return false;
        return horaChegada.plusMinutes(30).isBefore(horarioPrevisto);
    }

    @Override
    public String toString() {
        if (horarioPrevisto == null) {
            return super.toString();
        }
        return String.format("%s (%.5f, %.5f) @ %s",
                getNome(), getLatitude(), getLongitude(), horarioPrevisto);
    }
}
