package br.com.sosviale.controller.pontoColeta;

import br.com.sosviale.controller.pontoColeta.dto.PontoColetaRequest;
import br.com.sosviale.model.PontoColeta;

import java.util.List;

public interface PontoColetaController {

    void cadastrar(PontoColetaRequest request);

    void atualizar(PontoColetaRequest request);

    void excluir(Long id);

    PontoColeta buscarPorId(Long id);

    List<PontoColeta> listarTodos();
}
