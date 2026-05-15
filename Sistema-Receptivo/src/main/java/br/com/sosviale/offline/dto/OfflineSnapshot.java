package br.com.sosviale.offline.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Pacote completo de OS + transfers + passageiros para uso sem rede.
 */
public class OfflineSnapshot {

    private String usuario;
    private String syncedAt;
    private List<OfflineOrdemServicoDto> ordens = new ArrayList<>();

    public OfflineSnapshot() {}

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSyncedAt() { return syncedAt; }
    public void setSyncedAt(String syncedAt) { this.syncedAt = syncedAt; }

    public List<OfflineOrdemServicoDto> getOrdens() { return ordens; }
    public void setOrdens(List<OfflineOrdemServicoDto> ordens) {
        this.ordens = ordens != null ? ordens : new ArrayList<>();
    }
}
