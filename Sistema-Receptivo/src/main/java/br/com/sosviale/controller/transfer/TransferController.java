package br.com.sosviale.controller.transfer;

import br.com.sosviale.controller.transfer.dto.TransferRequest;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;

import java.util.List;

public interface TransferController {

    void cadastrar(TransferRequest request);

    void atualizar(TransferRequest request);

    void excluir(Integer id);

    List<Transfer> listarTodos();

    Transfer buscarPorId(Integer id);

    List<PontoColeta> listarPontosColeta();

    List<Passageiro> listarPassageiros();
}
