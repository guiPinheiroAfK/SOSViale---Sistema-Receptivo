package br.com.sosviale.offline;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.model.User;
import br.com.sosviale.offline.dto.OfflineSessionDto;
import br.com.sosviale.offline.dto.OfflineSnapshot;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.TransferService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sincroniza OS + passageiros do servidor para o disco local.
 */
public class OfflineSyncService {

    private final TransferService transferService = new TransferService();
    private final OrdemServicoService osService = new OrdemServicoService();
    private final OfflineStore store = OfflineStore.getInstance();

    /**
     * @return true se sincronizou com sucesso no servidor
     */
    public boolean syncFromServer(String usuario) {
        if (!ConnectivityService.isDatabaseOnline()) {
            return false;
        }
        try {
            List<Transfer> vinculados = transferService.listarVinculadosOrdemServico();
            Set<Integer> osIds = new LinkedHashSet<>();
            for (Transfer t : vinculados) {
                if (t.getOrdemServico() != null && t.getOrdemServico().getId() != null) {
                    osIds.add(t.getOrdemServico().getId());
                }
            }

            List<OrdemServico> ordens = new ArrayList<>();
            for (Integer osId : osIds) {
                OrdemServico os = osService.buscarComTransfers(osId);
                if (os != null) {
                    ordens.add(os);
                }
            }

            OfflineSnapshot snapshot = OfflineEntityMapper.fromOrdens(ordens, usuario);
            store.saveSnapshot(usuario, snapshot);
            return true;
        } catch (Exception e) {
            System.err.println("Falha na sincronização offline: " + e.getMessage());
            return false;
        }
    }

    public void saveSessionAfterLogin(User user) {
        if (user == null) return;
        OfflineSessionDto session = new OfflineSessionDto();
        session.setUsuario(user.getUsuario());
        session.setNome(user.getNome());
        session.setPerfil(user.getPerfil().name());
        session.setAdmin(user.isAdmin());
        try {
            store.saveSession(session);
        } catch (IOException e) {
            System.err.println("Não foi possível salvar sessão offline: " + e.getMessage());
        }
    }
}
