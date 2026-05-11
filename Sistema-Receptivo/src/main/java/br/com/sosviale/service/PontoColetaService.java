package br.com.sosviale.service;

import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.repository.PontoColetaRepository;

import java.util.List;

public class PontoColetaService {

    private final PontoColetaRepository repository = new PontoColetaRepository();

    public void cadastrar(PontoColeta ponto) {
        if (ponto == null) throw new IllegalArgumentException("Ponto de coleta não pode ser nulo.");
        if (ponto.getLocalColeta() == null || ponto.getLocalColeta().trim().isEmpty())
            throw new IllegalArgumentException("Local de coleta é obrigatório.");
        if (ponto.getTransfer() == null || ponto.getTransfer().getId() == null)
            throw new IllegalArgumentException("O ponto de coleta deve estar vinculado a um Transfer.");

        repository.salvar(ponto);
    }

    public List<PontoColeta> buscarPorTransfer(Integer transferId) {
        if (transferId == null || transferId <= 0) throw new IllegalArgumentException("ID do transfer inválido.");
        return repository.buscarPorTransfer(transferId);
    }

    public void atualizar(PontoColeta ponto) {
        if (ponto == null || ponto.getId() == null)
            throw new IllegalArgumentException("Ponto de coleta inválido para atualização.");
        repository.atualizar(ponto);
    }

    public void excluir(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public PontoColeta buscarPorId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        return repository.buscarPorId(id);
    }
}