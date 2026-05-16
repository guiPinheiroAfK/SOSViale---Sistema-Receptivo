package br.com.sosviale.service;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.repository.PassageiroRepository;

import java.util.List;

public class PassageiroService {

    private final PassageiroRepository repository = new PassageiroRepository();

    public void salvar(String nome, String documento, String nacionalidade) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome é obrigatório.");
        if (documento == null || documento.trim().isEmpty())
            throw new IllegalArgumentException("Documento é obrigatório.");
        if (nacionalidade == null || nacionalidade.trim().isEmpty())
            nacionalidade = "Brasileira";

        repository.salvar(new Passageiro(nome.trim(), documento.trim(), nacionalidade.trim()));
    }

    public void atualizar(Integer id, String nome, String documento, String nacionalidade) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome é obrigatório.");
        if (documento == null || documento.trim().isEmpty())
            throw new IllegalArgumentException("Documento é obrigatório.");
        if (nacionalidade == null || nacionalidade.trim().isEmpty())
            nacionalidade = "Brasileira";

        Passageiro p = new Passageiro(nome.trim(), documento.trim(), nacionalidade.trim());
        p.setId(id);
        repository.atualizar(p);
    }

    public void excluir(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public List<Passageiro> listarTodos() {
        return repository.listarTodos();
    }
}