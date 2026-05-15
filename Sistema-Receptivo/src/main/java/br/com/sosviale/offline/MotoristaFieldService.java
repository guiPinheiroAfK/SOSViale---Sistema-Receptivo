package br.com.sosviale.offline;

import br.com.sosviale.auth.SessionManager;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.offline.dto.OfflineSnapshot;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Fachada Offline First para a tela de serviços do motorista.
 */
public class MotoristaFieldService {

    public enum DataSource { ONLINE, OFFLINE_CACHE, EMPTY }

    private final TransferService transferService = new TransferService();
    private final OrdemServicoService osService = new OrdemServicoService();
    private final OfflineSyncService syncService = new OfflineSyncService();
    private final OfflineStore store = OfflineStore.getInstance();
    private final PendingSyncService pendingSync = new PendingSyncService();

    private DataSource lastSource = DataSource.EMPTY;
    private int lastPendingCount;

    public DataSource getLastSource() {
        return lastSource;
    }

    public int getLastPendingCount() {
        return lastPendingCount;
    }

    public boolean isOnline() {
        return ConnectivityService.isDatabaseOnline() && !SessionManager.getInstance().isModoOffline();
    }

    public String getUsuarioAtual() {
        return SessionManager.getInstance().getUsuarioAtual();
    }

    /**
     * Lista transfers vinculados à OS — online com fallback para cache local.
     */
    public List<Transfer> listarServicosAtivos() {
        String usuario = getUsuarioAtual();
        lastPendingCount = pendingSync.countPending(usuario);

        if (isOnline()) {
            try {
                pendingSync.flushPending(usuario);
                syncService.syncFromServer(usuario);
                List<Transfer> online = transferService.listarVinculadosOrdemServico();
                lastSource = DataSource.ONLINE;
                return online;
            } catch (Exception e) {
                // cai para cache
            }
        }

        Optional<OfflineSnapshot> snap = store.loadSnapshot(usuario);
        if (snap.isPresent()) {
            lastSource = DataSource.OFFLINE_CACHE;
            return OfflineEntityMapper.toTransfers(snap.get());
        }

        lastSource = DataSource.EMPTY;
        return new ArrayList<>();
    }

    public Transfer buscarTransfer(Integer id) {
        for (Transfer t : listarServicosAtivos()) {
            if (id.equals(t.getId())) {
                return t;
            }
        }
        return null;
    }

    public OrdemServico buscarOsComTransfers(Integer osId) {
        String usuario = getUsuarioAtual();

        if (isOnline()) {
            try {
                OrdemServico os = osService.buscarComTransfers(osId);
                if (os != null) {
                    syncService.syncFromServer(usuario);
                    return os;
                }
            } catch (Exception ignored) {
            }
        }

        return store.loadSnapshot(usuario)
                .flatMap(snap -> snap.getOrdens().stream()
                        .filter(o -> osId.equals(o.getId()))
                        .findFirst()
                        .map(OfflineEntityMapper::toOrdem))
                .orElse(null);
    }

    public void atualizarStatus(Integer transferId, StatusTransfer novoStatus) throws IOException {
        String usuario = getUsuarioAtual();

        if (isOnline()) {
            Transfer t = transferService.buscarPorId(transferId);
            if (t == null) throw new IllegalArgumentException("Transfer não encontrado.");
            t.setStatus(novoStatus);
            transferService.atualizar(t);
            syncService.syncFromServer(usuario);
            return;
        }

        Optional<OfflineSnapshot> opt = store.loadSnapshot(usuario);
        if (opt.isEmpty()) {
            throw new IllegalStateException("Sem dados offline. Conecte-se à rede e abra os serviços uma vez.");
        }
        OfflineSnapshot updated = OfflineEntityMapper.mergeStatusUpdate(opt.get(), transferId, novoStatus);
        store.saveSnapshot(usuario, updated);
        pendingSync.enqueueStatusUpdate(usuario, transferId, novoStatus);
        lastPendingCount = pendingSync.countPending(usuario);
    }

    public void excluirTransfer(Integer transferId) throws IOException {
        String usuario = getUsuarioAtual();

        if (isOnline()) {
            transferService.excluir(transferId);
            syncService.syncFromServer(usuario);
            return;
        }

        Optional<OfflineSnapshot> opt = store.loadSnapshot(usuario);
        if (opt.isEmpty()) {
            throw new IllegalStateException("Sem dados offline.");
        }
        OfflineSnapshot updated = OfflineEntityMapper.removeTransfer(opt.get(), transferId);
        store.saveSnapshot(usuario, updated);
        pendingSync.enqueueDelete(usuario, transferId);
        lastPendingCount = pendingSync.countPending(usuario);
    }

    public boolean forcarSincronizacao() {
        if (!isOnline()) return false;
        return syncService.syncFromServer(getUsuarioAtual());
    }

    public boolean hasLocalCache() {
        return store.hasSnapshot(getUsuarioAtual());
    }

    public Optional<String> getUltimaSincronizacao() {
        return store.loadSnapshot(getUsuarioAtual()).map(OfflineSnapshot::getSyncedAt);
    }
}
