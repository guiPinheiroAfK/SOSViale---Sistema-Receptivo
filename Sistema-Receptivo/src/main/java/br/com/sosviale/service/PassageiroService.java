package br.com.sosviale.service;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.TipoDocumento;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.util.DocumentoValidator;

import java.util.List;

public class PassageiroService {

    private final PassageiroRepository repository = new PassageiroRepository();

    public void salvar(String nome, String documento, TipoDocumento tipo, String nacionalidade) {
        // Validações básicas de preenchimento
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de documento é obrigatório.");
        }
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("Documento é obrigatório.");
        }

        // Validação de formato profissional usando o utilitário
        if (!DocumentoValidator.isValido(documento, tipo)) {
            throw new IllegalArgumentException("O formato do documento é inválido para " + tipo);
        }

        // Criando o passageiro com o novo construtor (4 parâmetros)
        // A lógica da nacionalidade "Brasileira" agora está dentro da Entity Passageiro
        repository.salvar(new Passageiro(nome.trim(), documento.trim(), tipo, nacionalidade));
    }

    public void atualizar(Integer id, String nome, String documento, TipoDocumento tipo, String nacionalidade) {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de documento é obrigatório.");
        }

        if (!DocumentoValidator.isValido(documento, tipo)) {
            throw new IllegalArgumentException("O formato do documento é inválido para " + tipo);
        }

        // Instancia com o novo construtor e define o ID para o merge do JPA
        Passageiro p = new Passageiro(nome.trim(), documento.trim(), tipo, nacionalidade);
        p.setId(id);

        repository.atualizar(p);
    }

    public void excluir(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido.");
        }
        repository.excluir(id);
    }

    public List<Passageiro> listarTodos() {
        return repository.listarTodos();
    }
}