package br.com.sosviale.service;

import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.TransferRepository;

import java.util.List;


public class TransferService {

    private final TransferRepository repository = new TransferRepository();

    public void cadastrar(Transfer transfer) {
        if (transfer == null) throw new IllegalArgumentException("Transfer não pode ser nulo.");

        if (transfer.getOrigem() == null || transfer.getOrigem().trim().isEmpty())
            throw new IllegalArgumentException("Origem é obrigatória.");

        if (transfer.getDestino() == null || transfer.getDestino().trim().isEmpty())
            throw new IllegalArgumentException("Destino é obrigatório.");

        // Validação atualizada para os novos campos
        if (transfer.getDataTransfer() == null)
            throw new IllegalArgumentException("A data do transfer é obrigatória.");

        if (transfer.getHoraTransfer() == null)
            throw new IllegalArgumentException("O horário do transfer é obrigatório.");

        repository.salvar(transfer);
    }

    public List<Transfer> listarTodos() {
        return repository.listarTodos();
    }

    public void atualizar(Transfer transfer) {
        if (transfer == null || transfer.getId() == null)
            throw new IllegalArgumentException("Transfer inválido para atualização.");
        repository.atualizar(transfer);
    }

    public void excluir(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public int contarPassageirosPorVeiculo(Integer veiculoId) {
        if (veiculoId == null || veiculoId <= 0)
            throw new IllegalArgumentException("ID do veículo inválido.");
        return repository.contarPassageirosPorVeiculo(veiculoId);
    }

    public Transfer buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        return repository.buscarPorId(id);
    }

    public long contarSemOrdemServico() {
        return repository.contarSemOrdemServico();
    }
}