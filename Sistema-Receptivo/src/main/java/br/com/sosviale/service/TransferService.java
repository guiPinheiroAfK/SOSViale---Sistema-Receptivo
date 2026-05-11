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
        if (transfer.getDataHora() == null)
            throw new IllegalArgumentException("Data e hora são obrigatórias.");

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

    public void excluir(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public int contarPassageirosPorVeiculo(Long veiculoId) {
        if (veiculoId == null || veiculoId <= 0) throw new IllegalArgumentException("ID do veículo inválido.");
        return repository.contarPassageirosPorVeiculo(veiculoId);
    }

    public Transfer buscarPorId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        return repository.buscarPorId(id);
    }

    public long contarSemOrdemServico() {
        return repository.contarSemOrdemServico();
    }
}