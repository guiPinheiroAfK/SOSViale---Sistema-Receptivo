package br.com.sosviale.offline;

import br.com.sosviale.offline.dto.PendingOperationDto;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.service.TransferService;
import br.com.sosviale.model.Transfer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

// fila pendente quando fez update/delete sem rede; flush manda pro transferService

public class PendingSyncService {

    private final OfflineStore store = OfflineStore.getInstance();
    private final TransferService transferService = new TransferService();

    public void enqueueStatusUpdate(String usuario, int transferId, StatusTransfer status) throws IOException {
        List<PendingOperationDto> pending = store.loadPending(usuario);
        PendingOperationDto op = new PendingOperationDto();
        op.setId(UUID.randomUUID().toString());
        op.setType(PendingOperationDto.Type.UPDATE_TRANSFER_STATUS);
        op.setTransferId(transferId);
        op.setStatus(status.name());
        op.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        pending.add(op);
        store.savePending(usuario, pending);
    }

    public void enqueueDelete(String usuario, int transferId) throws IOException {
        List<PendingOperationDto> pending = store.loadPending(usuario);
        PendingOperationDto op = new PendingOperationDto();
        op.setId(UUID.randomUUID().toString());
        op.setType(PendingOperationDto.Type.DELETE_TRANSFER);
        op.setTransferId(transferId);
        op.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        pending.add(op);
        store.savePending(usuario, pending);
    }

    // aplica ate esgotar ou erro; remove da fila o que passou

    public int flushPending(String usuario) {
        if (!ConnectivityService.isDatabaseOnline()) {
            return 0;
        }
        List<PendingOperationDto> pending = new ArrayList<>(store.loadPending(usuario));
        if (pending.isEmpty()) return 0;

        int applied = 0;
        Iterator<PendingOperationDto> it = pending.iterator();
        while (it.hasNext()) {
            PendingOperationDto op = it.next();
            try {
                switch (op.getType()) {
                    case UPDATE_TRANSFER_STATUS -> {
                        Transfer t = transferService.buscarPorId(op.getTransferId());
                        if (t != null) {
                            t.setStatus(StatusTransfer.valueOf(op.getStatus()));
                            transferService.atualizar(t);
                        }
                    }
                    case DELETE_TRANSFER -> transferService.excluir(op.getTransferId());
                    default -> { }
                }
                it.remove();
                applied++;
            } catch (Exception e) {
                System.err.println("Pendência não aplicada: " + op.getId() + " — " + e.getMessage());
            }
        }
        try {
            store.savePending(usuario, pending);
        } catch (IOException e) {
            System.err.println("Erro ao atualizar fila pendente: " + e.getMessage());
        }
        return applied;
    }

    public int countPending(String usuario) {
        return store.loadPending(usuario).size();
    }
}
