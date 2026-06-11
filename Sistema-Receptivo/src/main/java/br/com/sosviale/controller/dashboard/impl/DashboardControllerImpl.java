package br.com.sosviale.controller.dashboard.impl;

import br.com.sosviale.controller.dashboard.DashboardController;
import br.com.sosviale.controller.dashboard.dto.DashboardResponse;
import br.com.sosviale.service.DashboardService;

public class DashboardControllerImpl implements DashboardController {

    private final DashboardService dashboardService;

    public DashboardControllerImpl(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public DashboardResponse carregarMetricas() {
        DashboardService.DashboardSnapshot snap = dashboardService.carregarSnapshot();

        return new DashboardResponse(
                snap.totalPassageiros(),
                snap.totalMotoristas(),
                snap.totalVeiculos(),
                snap.transfersSemOS(),
                snap.totalOs(),
                snap.transfersHoje(),
                snap.transfersComOs(),
                snap.emExecucao(),
                snap.proximosTransfers()
        );
    }
}
