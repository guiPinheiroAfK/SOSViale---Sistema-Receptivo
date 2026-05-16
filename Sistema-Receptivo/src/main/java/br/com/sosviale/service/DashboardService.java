package br.com.sosviale.service;

import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.repository.TransferRepository;
import br.com.sosviale.repository.VeiculoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Agrega métricas e listas exibidas no painel inicial (View → Service → Repository).

public class DashboardService {

    private final PassageiroRepository passageiroRepository = new PassageiroRepository();
    private final MotoristaRepository motoristaRepository = new MotoristaRepository();
    private final VeiculoRepository veiculoRepository = new VeiculoRepository();
    private final TransferRepository transferRepository = new TransferRepository();
    private final TransferService transferService = new TransferService();

    public DashboardSnapshot carregarSnapshot() {
        long totalPassageiros = passageiroRepository.contar();
        long totalMotoristas = motoristaRepository.contar();
        long totalVeiculos = veiculoRepository.contar();
        long transfersSemOS = transferRepository.contarSemOrdemServico();

        List<Transfer> todos = transferService.listarTodos();
        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

        long transfersHoje = todos.stream()
                .filter(t -> hoje.equals(t.getDataTransfer()))
                .count();
        long transfersComOs = todos.stream()
                .filter(t -> t.getOrdemServico() != null)
                .count();
        long emExecucao = todos.stream()
                .filter(t -> t.getStatus() == StatusTransfer.EM_EXECUCAO)
                .count();
        long totalOs = todos.stream()
                .filter(t -> t.getOrdemServico() != null)
                .map(t -> t.getOrdemServico().getId())
                .distinct()
                .count();

        List<Transfer> proximos = todos.stream()
                .filter(t -> t.getDataTransfer() != null && t.getHoraTransfer() != null)
                .filter(t -> {
                    LocalDateTime dt = LocalDateTime.of(t.getDataTransfer(), t.getHoraTransfer());
                    return !dt.isBefore(agora);
                })
                .sorted(Comparator.comparing(t ->
                        LocalDateTime.of(t.getDataTransfer(), t.getHoraTransfer())))
                .limit(8)
                .collect(Collectors.toList());

        return new DashboardSnapshot(
                totalPassageiros,
                totalMotoristas,
                totalVeiculos,
                transfersSemOS,
                totalOs,
                transfersHoje,
                transfersComOs,
                emExecucao,
                proximos
        );
    }

    public record DashboardSnapshot(
            long totalPassageiros,
            long totalMotoristas,
            long totalVeiculos,
            long transfersSemOS,
            long totalOs,
            long transfersHoje,
            long transfersComOs,
            long emExecucao,
            List<Transfer> proximosTransfers
    ) {
    }
}
