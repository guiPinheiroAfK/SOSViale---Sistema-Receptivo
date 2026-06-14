package br.com.sosviale.controller.passageiro.impl;

import br.com.sosviale.controller.passageiro.PassageiroController;
import br.com.sosviale.controller.passageiro.dto.PassageiroRequest;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.service.PassageiroService;

import java.util.List;

public class PassageiroControllerImpl implements PassageiroController {

    private final PassageiroService passageiroService;

    public PassageiroControllerImpl(PassageiroService passageiroService) {
        this.passageiroService = passageiroService;
    }

    @Override
    public void salvar(PassageiroRequest request) {
        passageiroService.salvar(request.nome(), request.documento(), request.tipoDocumento(), request.nacionalidade());
    }

    @Override
    public void atualizar(PassageiroRequest request) {
        passageiroService.atualizar(request.id(), request.nome(), request.documento(), request.tipoDocumento(), request.nacionalidade());
    }

    @Override
    public void excluir(Integer id) {
        passageiroService.excluir(id);
    }

    @Override
    public List<Passageiro> listarTodos() {
        return passageiroService.listarTodos();
    }

    @Override
    public Passageiro buscarPorId(Integer id) {
        return passageiroService.buscarPorId(id);
    }
}
