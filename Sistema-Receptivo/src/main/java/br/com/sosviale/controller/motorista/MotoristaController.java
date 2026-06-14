package br.com.sosviale.controller.motorista;

import br.com.sosviale.controller.motorista.dto.MotoristaRequest;
import br.com.sosviale.model.Motorista;

import java.util.List;

public interface MotoristaController {

    void salvar(MotoristaRequest request);

    void atualizar(MotoristaRequest request);

    void excluir(Integer id);

    List<Motorista> listarTodos();
}
