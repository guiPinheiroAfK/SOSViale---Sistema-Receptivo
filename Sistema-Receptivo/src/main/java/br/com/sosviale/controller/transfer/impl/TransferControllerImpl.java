package br.com.sosviale.controller.transfer.impl;

import br.com.sosviale.controller.transfer.TransferController;
import br.com.sosviale.controller.transfer.dto.TransferRequest;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.service.PassageiroService;
import br.com.sosviale.service.PontoColetaService;
import br.com.sosviale.service.TransferService;

import java.util.ArrayList;
import java.util.List;

public class TransferControllerImpl implements TransferController {

    private final TransferService transferService;
    private final PontoColetaService pontoColetaService;
    private final PassageiroService passageiroService;

    public TransferControllerImpl(TransferService transferService,
                                  PontoColetaService pontoColetaService,
                                  PassageiroService passageiroService) {
        this.transferService = transferService;
        this.pontoColetaService = pontoColetaService;
        this.passageiroService = passageiroService;
    }

    @Override
    public void cadastrar(TransferRequest request) {
        Transfer t = new Transfer();
        preencherTransfer(t, request);
        transferService.cadastrar(t);
    }

    @Override
    public void atualizar(TransferRequest request) {
        Transfer t = transferService.buscarPorId(request.id());
        preencherTransfer(t, request);
        transferService.atualizar(t);
    }

    @Override
    public void excluir(Integer id) {
        transferService.excluir(id);
    }

    @Override
    public List<Transfer> listarTodos() {
        return transferService.listarTodos();
    }

    @Override
    public Transfer buscarPorId(Integer id) {
        return transferService.buscarPorId(id);
    }

    @Override
    public List<PontoColeta> listarPontosColeta() {
        return pontoColetaService.listarTodos();
    }

    @Override
    public List<Passageiro> listarPassageiros() {
        return passageiroService.listarTodos();
    }

    private void preencherTransfer(Transfer t, TransferRequest req) {
        t.setOrigem(req.origem());
        t.setDestino(req.destino());
        t.setDataTransfer(req.data());
        t.setHoraTransfer(req.hora());
        t.setValorOriginal(req.valorOriginal());
        t.setMoedaOrigem(req.moeda());
        t.setStatus(req.status());
        t.setPassageiros(new ArrayList<>(req.passageiros()));
    }
}
