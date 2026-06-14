package br.com.sosviale.controller.ordemservico.dto;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.Veiculo;

import java.time.LocalDate;

public record OrdemServicoRequest(
        Motorista motorista,
        Veiculo veiculo,
        LocalDate dataServico
) {}
