package br.com.sosviale.controller.ordemservico.impl;

import br.com.sosviale.controller.ordemservico.OrdemServicoController;
import br.com.sosviale.controller.ordemservico.dto.OrdemServicoRequest;
import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.service.MotoristaService;
import br.com.sosviale.service.OrdemServicoService;
import br.com.sosviale.service.PathFindingTimeWindow;
import br.com.sosviale.service.TransferService;
import br.com.sosviale.service.VeiculoService;
import br.com.sosviale.service.pathfinding.RouteResult;

import java.util.List;

public class OrdemServicoControllerImpl implements OrdemServicoController {

    private final OrdemServicoService osService;
    private final MotoristaService motoristaService;
    private final VeiculoService veiculoService;
    private final TransferService transferService;

    public OrdemServicoControllerImpl(OrdemServicoService osService,
                                       MotoristaService motoristaService,
                                       VeiculoService veiculoService,
                                       TransferService transferService) {
        this.osService = osService;
        this.motoristaService = motoristaService;
        this.veiculoService = veiculoService;
        this.transferService = transferService;
    }

    @Override
    public void criar(OrdemServicoRequest request) {
        OrdemServico os = new OrdemServico();
        os.setDataServico(request.dataServico());
        os.setMotorista(request.motorista());
        os.setVeiculo(request.veiculo());
        osService.cadastrar(os);
    }

    @Override
    public OrdemServico buscarComTransfers(Integer id) {
        return osService.buscarComTransfers(id);
    }

    @Override
    public List<OrdemServico> listarTodos() {
        return osService.listarTodos();
    }

    @Override
    public void vincularTransfer(Integer transferId, OrdemServico os) {
        transferService.vincularAOS(transferId, os);
    }

    @Override
    public Transfer buscarTransferPorId(Integer id) {
        return transferService.buscarPorId(id);
    }

    @Override
    public List<Transfer> listarTransfersDisponiveis() {
        return transferService.listarTodos().stream()
                .filter(t -> t.getOrdemServico() == null)
                .toList();
    }

    @Override
    public List<Motorista> listarMotoristas() {
        return motoristaService.listarTodos();
    }

    @Override
    public List<Veiculo> listarVeiculos() {
        return veiculoService.listarTodos();
    }

    @Override
    public RouteResult otimizarRota(OrdemServico os) {
        return PathFindingTimeWindow.otimizarComTimeWindow(os);
    }
}
