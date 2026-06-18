package br.com.sosviale.controller.motorista.impl;

import br.com.sosviale.controller.motorista.MotoristaController;
import br.com.sosviale.controller.motorista.dto.MotoristaRequest;
import br.com.sosviale.model.Motorista;
import br.com.sosviale.service.MotoristaService;

import java.util.List;

public class MotoristaControllerImpl implements MotoristaController {

    private final MotoristaService motoristaService;

    public MotoristaControllerImpl(MotoristaService motoristaService) {
        this.motoristaService = motoristaService;
    }

    @Override
    public void salvar(MotoristaRequest request) {
        motoristaService.salvar(request.nome(), request.cnh(), request.telefone());
    }

    @Override
    public void atualizar(MotoristaRequest request) {
        motoristaService.atualizar(request.id(), request.nome(), request.cnh(), request.telefone());
    }

    @Override
    public void excluir(Integer id) {
        motoristaService.excluir(id);
    }

    @Override
    public List<Motorista> listarTodos() {
        return motoristaService.listarTodos();
    }
}
