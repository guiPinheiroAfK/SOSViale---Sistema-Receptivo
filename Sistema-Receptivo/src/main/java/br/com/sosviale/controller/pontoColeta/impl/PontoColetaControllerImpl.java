package br.com.sosviale.controller.pontoColeta.impl;

import br.com.sosviale.controller.pontoColeta.PontoColetaController;
import br.com.sosviale.controller.pontoColeta.dto.PontoColetaRequest;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.service.PontoColetaService;

import java.util.List;

public class PontoColetaControllerImpl implements PontoColetaController {

    private final PontoColetaService pontoColetaService;

    public PontoColetaControllerImpl(PontoColetaService pontoColetaService) {
        this.pontoColetaService = pontoColetaService;
    }

    @Override
    public void cadastrar(PontoColetaRequest request) {
        PontoColeta pc = new PontoColeta();
        preencherPontoColeta(pc, request);
        pontoColetaService.cadastrar(pc);
    }

    @Override
    public void atualizar(PontoColetaRequest request) {
        PontoColeta pc = pontoColetaService.buscarPorId(request.id());
        preencherPontoColeta(pc, request);
        pontoColetaService.atualizar(pc);
    }

    @Override
    public void excluir(Long id) {
        pontoColetaService.excluir(id);
    }

    @Override
    public PontoColeta buscarPorId(Long id) {
        return pontoColetaService.buscarPorId(id);
    }

    @Override
    public List<PontoColeta> listarTodos() {
        return pontoColetaService.listarTodos();
    }

    private void preencherPontoColeta(PontoColeta pc, PontoColetaRequest req) {
        pc.setLocalColeta(req.localColeta());
        pc.setLatitude(req.latitude());
        pc.setLongitude(req.longitude());
    }
}
