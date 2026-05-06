package br.com.sosviale.util;

import br.com.sosviale.model.*;
import br.com.sosviale.repository.*;
import org.jline.reader.LineReader;

public class Excluir {

    // repositórios injetados pelo construtor
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private OrdemServicoRepository osRepo;

    // construtor que recebe os repositórios instanciados pela Main
    public Excluir(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo,
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
    public void excluirTransfer(LineReader reader) {
        try {
            Long id = AuxiliarUtils.lerIdValido(reader, "ID do Transfer para cancelar: ");
            Transfer t = transferRepo.buscarPorId(id);
            if (t == null) {
                System.out.println("\u001B[31mTransfer não encontrado.\u001B[0m");
                return;
            }

            String conf = reader.readLine("Confirmar exclusão do Transfer #" + id + "? (s/n): ").trim();
            if (conf.equalsIgnoreCase("s")) {
                transferRepo.excluir(id);
                System.out.println("\u001B[32m✔ Transfer removido.\u001B[0m");
            } else {
                System.out.println("Operação cancelada.");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro ao excluir transfer: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Passageiros
    public void excluirPassageiro(LineReader reader) {
        System.out.println("\n\u001B[31m--- EXCLUIR PASSAGEIRO --- \u001B[0m");
        try {
            Long id = AuxiliarUtils.lerIdValido(reader, "ID do passageiro para excluir: ");
            Passageiro p = passageiroRepo.buscarPorId(id);
            if (p == null) {
                System.out.println("Passageiro não encontrado!");
                return;
            }
            String conf = reader.readLine("Tem certeza que deseja excluir " + p.getNome() + "? (s/n): ").trim();
            if (conf.equalsIgnoreCase("s")) {
                passageiroRepo.excluir(id);
                System.out.println("\u001B[32m✔ Passageiro removido com sucesso!\u001B[0m");
            } else {
                System.out.println("Operação cancelada.");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Veículo
    public void excluirVeiculo(LineReader reader) {
        try {
            Long id = AuxiliarUtils.lerIdValido(reader, "ID para excluir: ");
            Veiculo v = veiculoRepo.buscarPorId(id);
            if (v == null) {
                System.out.println("\u001B[31mVeículo não encontrado.\u001B[0m");
                return;
            }
            String conf = reader.readLine("Tem certeza que deseja excluir " + v.getLabel() + "? (s/n): ").trim();
            if (conf.equalsIgnoreCase("s")) {
                veiculoRepo.excluir(id);
                System.out.println("\u001B[32m✔ Veículo removido!\u001B[0m");
            } else {
                System.out.println("Operação cancelada.");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro: veículo pode estar em uso em uma OS.\u001B[0m");
        }
    }

    // Motoristas
    public void excluirMotorista(LineReader reader) {
        System.out.println("\n\u001B[31m--- EXCLUIR MOTORISTA --- \u001B[0m");
        try {
            Long id = AuxiliarUtils.lerIdValido(reader, "ID para excluir: ");
            Motorista m = motoristaRepo.buscarPorId(id);
            if (m == null) {
                System.out.println("Motorista não encontrado.");
                return;
            }
            String conf = reader.readLine("Excluir " + m.getNome() + "? (s/n): ").trim();
            if (conf.equalsIgnoreCase("s")) {
                motoristaRepo.excluir(id);
                System.out.println("\u001B[32m✔ Motorista removido!\u001B[0m");
            } else {
                System.out.println("Operação cancelada.");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: não foi possível excluir (motorista pode estar vinculado a uma OS).\u001B[0m");
        }
    }

}
