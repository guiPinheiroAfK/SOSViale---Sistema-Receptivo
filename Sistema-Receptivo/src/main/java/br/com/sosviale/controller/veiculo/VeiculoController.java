package br.com.sosviale.controller.veiculo;

import br.com.sosviale.controller.veiculo.dto.VeiculoRequest;
import br.com.sosviale.model.Veiculo;

import java.util.List;

public interface VeiculoController {

    void salvar(VeiculoRequest request);

    void atualizar(VeiculoRequest request);

    void excluir(Integer id);

    List<Veiculo> listarTodos();
}
