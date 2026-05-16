package br.com.sosviale.offline.dto;

import java.util.ArrayList;
import java.util.List;

// o que vai em snapshot.json — todas as OS visiveis pro usuario

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
