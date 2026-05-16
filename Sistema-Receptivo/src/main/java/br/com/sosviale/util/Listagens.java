package br.com.sosviale.util;

import br.com.sosviale.model.*;
import br.com.sosviale.repository.*;

import java.util.List;

public class Listagens {

    // repositórios injetados pelo construtor
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private OrdemServicoRepository osRepo;

    // construtor que recebe os repositórios instanciados pela Main
    public Listagens(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo,
                        TransferRepository transferRepo, MotoristaRepository motoristaRepo,
                        PontoColetaRepository pontoColetaRepo, OrdemServicoRepository osRepo) {
        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
        this.motoristaRepo = motoristaRepo;
        this.pontoColetaRepo = pontoColetaRepo;
        this.osRepo = osRepo;
    }

    // Transfers
    public void listarTransfers() {
        System.out.println("\n\u001B[36m--- LISTA DE TRANSFERS --- \u001B[0m");
        try {
            List<Transfer> lista = transferRepo.listarTodos();
            if (lista.isEmpty()) {
                System.out.println("Nenhum agendamento encontrado.");
                return;
            }

            System.out.println(String.format("%-4s | %-12s | %-15s | %-15s | %-20s | %-25s | %-10s",
                    "ID", "STATUS", "ORIGEM", "DESTINO", "SITUAÇÃO OS", "PASSAGEIROS", "VALOR"));
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            for (Transfer t : lista) {
                String nomesPassageiros = t.getPassageiros().stream()
                        .map(Passageiro::getNome)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Nenhum");

                // exibe qual OS e motorista estão vinculados ao transfer
                String situacaoOs = (t.getOrdemServico() != null && t.getOrdemServico().getMotorista() != null)
                        ? "OS #" + t.getOrdemServico().getId() + " - " + t.getOrdemServico().getMotorista().getNome()
                        : "Não atribuído";

                System.out.println(String.format("%-4d | %-12s | %-15s | %-15s | %-20s | %-25s | R$%-10.2f",
                        t.getId(), t.getStatus(), t.getOrigem(), t.getDestino(),
                        situacaoOs, nomesPassageiros, t.getValorBase()));
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro ao listar: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Motoristas
    public void listarMotoristas() {
        System.out.println("\n\u001B[36m--- MOTORISTAS CADASTRADOS --- \u001B[0m");
        try {
            List<Motorista> lista = motoristaRepo.listarTodos();
            if (lista.isEmpty()) {
                System.out.println("Nenhum motorista cadastrado.");
                return;
            }
            System.out.println(String.format("%-5s | %-25s | %-15s", "ID", "NOME", "CNH"));
            for (Motorista m : lista) {
                System.out.println(String.format("%-5d | %-25s | %-15s", m.getId(), m.getNome(), m.getCnh()));
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO LISTAR]: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Passageiros
    public void listarPassageiros() {
        System.out.println("\n\u001B[36m--- LISTA DE PASSAGEIROS CADASTRADOS --- \u001B[0m");
        try {
            List<Passageiro> lista = passageiroRepo.listarTodos();
            if (lista.isEmpty()) {
                System.out.println("\u001B[33mNenhum passageiro encontrado.\u001B[0m");
                return;
            }
            System.out.println(String.format("%-5s | %-25s | %-15s | %-15s", "ID", "NOME", "DOCUMENTO", "NACIONALIDADE"));
            System.out.println("-------------------------------------------------------------------------");
            for (Passageiro p : lista) {
                System.out.println(String.format("%-5d | %-25s | %-15s | %-15s",
                        p.getId(), p.getNome(), p.getDocumento(), p.getNacionalidade()));
            }
            System.out.println("-------------------------------------------------------------------------");
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO LISTAR]: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Veículos
    public void listarVeiculos() {
        System.out.println("\n\u001B[36m--- FROTA DE VEÍCULOS --- \u001B[0m");
        try {
            List<Veiculo> lista = veiculoRepo.listarTodos();
            if (lista.isEmpty()) {
                System.out.println("Nenhum veículo cadastrado.");
                return;
            }
            System.out.println(String.format("%-5s | %-20s | %-10s | %-10s", "ID", "MODELO", "PLACA", "CAPACIDADE"));
            System.out.println("------------------------------------------------------------");
            for (Veiculo v : lista) {
                System.out.println(String.format("%-5d | %-20s | %-10s | %-10d",
                        v.getId(), v.getLabel(), v.getPlaca(), v.getCapacidade()));
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO LISTAR]: " + e.getMessage() + "\u001B[0m");
        }
    }

    //
}
