package br.com.sosviale.controller.veiculo.impl;

import br.com.sosviale.controller.veiculo.VeiculoController;
import br.com.sosviale.controller.veiculo.dto.VeiculoRequest;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.service.VeiculoService;

import java.util.List;

public class VeiculoControllerImpl implements VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoControllerImpl(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @Override
    public void salvar(VeiculoRequest request) {
        veiculoService.salvar(request.label(), request.placa(), request.capacidade(), request.marca(), request.tipo());
    }

    @Override
    public void atualizar(VeiculoRequest request) {
        veiculoService.atualizar(request.id(), request.label(), request.placa(), request.capacidade(), request.marca(), request.tipo());
    }

    @Override
    public void excluir(Integer id) {
        veiculoService.excluir(id);
    }

    @Override
    public List<Veiculo> listarTodos() {
        return veiculoService.listarTodos();
    }
}
