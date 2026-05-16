package br.com.sosviale.service;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.repository.MotoristaRepository;

import java.util.List;

public class MotoristaService {

    private final MotoristaRepository repository = new MotoristaRepository();

    public void salvar(String nome, String cnh) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome é obrigatório.");
        if (cnh == null || cnh.trim().isEmpty())
            throw new IllegalArgumentException("CNH é obrigatória.");

        repository.salvar(new Motorista(nome.trim(), cnh.trim()));
    }

    public void atualizar(Integer id, String nome, String cnh) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome é obrigatório.");
        if (cnh == null || cnh.trim().isEmpty())
            throw new IllegalArgumentException("CNH é obrigatória.");

        Motorista m = new Motorista(nome.trim(), cnh.trim());
        m.setId(id);
        repository.atualizar(m);
    }

    public void excluir(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public List<Motorista> listarTodos() {
        return repository.listarTodos();
    }
}