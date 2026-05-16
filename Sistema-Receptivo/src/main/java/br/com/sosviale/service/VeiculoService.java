package br.com.sosviale.service;

import br.com.sosviale.model.Veiculo;
import br.com.sosviale.repository.VeiculoRepository;

import java.util.List;

public class VeiculoService {

    private final VeiculoRepository repository = new VeiculoRepository();

    public void salvar(String label, String placa, Integer capacidade) {
        if (label == null || label.trim().isEmpty())
            throw new IllegalArgumentException("Modelo é obrigatório.");
        if (placa == null || placa.trim().isEmpty())
            throw new IllegalArgumentException("Placa é obrigatória.");
        if (capacidade == null || capacidade <= 0)
            throw new IllegalArgumentException("Capacidade deve ser maior que zero.");

        repository.salvar(new Veiculo(label.trim(), placa.trim().toUpperCase(), capacidade));
    }

    public void atualizar(Integer id, String label, String placa, Integer capacidade) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        if (label == null || label.trim().isEmpty())
            throw new IllegalArgumentException("Modelo é obrigatório.");
        if (placa == null || placa.trim().isEmpty())
            throw new IllegalArgumentException("Placa é obrigatória.");
        if (capacidade == null || capacidade <= 0)
            throw new IllegalArgumentException("Capacidade deve ser maior que zero.");

        Veiculo v = new Veiculo(label.trim(), placa.trim().toUpperCase(), capacidade);
        v.setId(id);
        repository.atualizar(v);
    }

    public void excluir(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public List<Veiculo> listarTodos() {
        return repository.listarTodos();
    }
}