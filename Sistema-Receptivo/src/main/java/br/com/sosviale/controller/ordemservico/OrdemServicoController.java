package br.com.sosviale.controller.ordemservico;

import br.com.sosviale.controller.ordemservico.dto.OrdemServicoRequest;
import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.service.pathfinding.RouteResult;

import java.util.List;

public interface OrdemServicoController {

    void criar(OrdemServicoRequest request);

    OrdemServico buscarComTransfers(Integer id);

    List<OrdemServico> listarTodos();

    void vincularTransfer(Integer transferId, OrdemServico os);

    Transfer buscarTransferPorId(Integer id);

    List<Transfer> listarTransfersDisponiveis();

    List<Motorista> listarMotoristas();

    List<Veiculo> listarVeiculos();

    RouteResult otimizarRota(OrdemServico os);
}
