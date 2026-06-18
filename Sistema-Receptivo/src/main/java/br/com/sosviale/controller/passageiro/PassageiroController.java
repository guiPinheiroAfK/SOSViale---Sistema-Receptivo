package br.com.sosviale.controller.passageiro;

import br.com.sosviale.controller.passageiro.dto.PassageiroRequest;
import br.com.sosviale.model.Passageiro;

import java.util.List;

public interface PassageiroController {

    void salvar(PassageiroRequest request);

    void atualizar(PassageiroRequest request);

    void excluir(Integer id);

    List<Passageiro> listarTodos();

    Passageiro buscarPorId(Integer id);
}
