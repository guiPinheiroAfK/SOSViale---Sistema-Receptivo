package br.com.sosviale.service;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.repository.MotoristaRepository;

import java.util.List;

public class MotoristaService {

    private final MotoristaRepository repository = new MotoristaRepository();

    public void salvar(String nome, String cnh) {
        salvar(nome, cnh, null);
    }

    public void salvar(String nome, String cnh, String telefone) {
        validarCampos(nome, cnh);
        Motorista m = new Motorista(nome.trim(), cnh.trim());
        m.setTelefone(normalizarTelefone(telefone));
        repository.salvar(m);
    }

    public void atualizar(Integer id, String nome, String cnh) {
        atualizar(id, nome, cnh, null);
    }

    public void atualizar(Integer id, String nome, String cnh, String telefone) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        validarCampos(nome, cnh);

        Motorista m = new Motorista(nome.trim(), cnh.trim());
        m.setId(id);
        m.setTelefone(normalizarTelefone(telefone));
        repository.atualizar(m);
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null || telefone.isBlank()) return null;
        return telefone.trim();
    }

    public void excluir(Integer id) {
        if (id == null) throw new IllegalArgumentException("ID inválido.");
        repository.excluir(id);
    }

    public List<Motorista> listarTodos() {
        return repository.listarTodos();
    }

    private void validarCampos(String nome, String cnh) {
        if (nome == null || nome.trim().isEmpty())
            throw new IllegalArgumentException("Nome é obrigatório.");
        if (cnh == null || cnh.trim().isEmpty())
            throw new IllegalArgumentException("CNH é obrigatória.");

        String cnhLimpa = cnh.replaceAll("[^0-9]", "");
        if (cnhLimpa.length() != 11)
            throw new IllegalArgumentException("CNH inválida. Deve conter 11 dígitos numéricos.");
        if (cnhLimpa.matches("(\\d)\\1{10}"))
            throw new IllegalArgumentException("CNH inválida. Sequência de dígitos repetidos não é permitida.");
    }
}