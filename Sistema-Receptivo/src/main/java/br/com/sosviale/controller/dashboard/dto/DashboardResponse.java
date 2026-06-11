package br.com.sosviale.controller.dashboard.dto;

import br.com.sosviale.model.Transfer;

import java.util.List;

public record DashboardResponse(
        long totalPassageiros,
        long totalMotoristas,
        long totalVeiculos,
        long transfersSemOS,
        long totalOs,
        long transfersHoje,
        long transfersComOs,
        long emExecucao,
        List<Transfer> proximosTransfers
) {}
