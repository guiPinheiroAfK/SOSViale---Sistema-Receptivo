package br.com.sosviale.util;

import br.com.sosviale.model.*;
import br.com.sosviale.repository.*;
import br.com.sosviale.service.StatusTransfer;
import org.jline.reader.LineReader;

import java.math.BigDecimal;

public class Editar {

    // repositórios injetados pelo construtor
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private OrdemServicoRepository osRepo;

    // construtor que recebe os repositórios instanciados pela Main
    public Editar(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo,
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
    public void editarTransfer(LineReader reader) {
        try {
            Long id = AuxiliarUtils.lerIdValido(reader, "ID do Transfer para editar: ");
            Transfer t = transferRepo.buscarPorId(id);

            if (t == null) {
                System.out.println("\u001B[31mAgendamento não encontrado.\u001B[0m");
                return;
            }

            System.out.println("\n\u001B[33m(Pressione ENTER para manter o valor atual)\u001B[0m");

            String novaOrigem = reader.readLine("Nova Origem [" + t.getOrigem() + "]: ").trim();
            if (!novaOrigem.isEmpty()) t.setOrigem(novaOrigem);

            String novoDestino = reader.readLine("Novo Destino [" + t.getDestino() + "]: ").trim();
            if (!novoDestino.isEmpty()) t.setDestino(novoDestino);

            // valida que origem e destino não ficaram iguais após edição
            if (t.getOrigem().equalsIgnoreCase(t.getDestino())) {
                System.out.println("\u001B[31mOridem e destino não podem ser iguais. Edição cancelada.\u001B[0m");
                return;
            }

            String novoValor = reader.readLine("Novo Valor [R$ " + t.getValorBase() + "]: ").trim();
            if (!novoValor.isEmpty()) {
                try {
                    BigDecimal valor = new BigDecimal(novoValor.replace(",", "."));
                    if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                        System.out.println("\u001B[31mValor deve ser maior que zero. Mantendo o anterior.\u001B[0m");
                    } else {
                        t.setValorBase(valor);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\u001B[31mValor inválido. Mantendo o anterior.\u001B[0m");
                }
            }

            System.out.println("\nStatus atual: " + t.getStatus());
            System.out.println("Escolha o novo status:");
            System.out.println("1. AGENDADO  2. EM_ANDAMENTO  3. CONCLUIDO  4. CANCELADO");
            System.out.println("Enter para manter o mesmo.");

            String opStatus = reader.readLine("> ").trim();
            switch (opStatus) {
                case "1" -> t.setStatus(StatusTransfer.AGENDADO);
                case "2" -> t.setStatus(StatusTransfer.EM_ANDAMENTO);
                case "3" -> t.setStatus(StatusTransfer.CONCLUIDO);
                case "4" -> t.setStatus(StatusTransfer.CANCELADO);
            }

            transferRepo.atualizar(t);
            System.out.println("\n\u001B[32m✔ Transfer #" + t.getId() + " atualizado com sucesso!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO NA EDIÇÃO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Passageiros
    public void editarPassageiro(LineReader reader) {
        System.out.println("\n\u001B[34m--- EDITAR PASSAGEIRO --- \u001B[0m");
        try {
            Integer id = AuxiliarUtils.lerIdValido(reader, "ID do passageiro para excluir: ").intValue();
            Passageiro p = passageiroRepo.buscarPorId(id);
            if (p == null) {
                System.out.println("Passageiro não encontrado.");
                return;
            }

            String novoNome = reader.readLine("Novo Nome [" + p.getNome() + "]: ").trim();
            if (!novoNome.isEmpty()) {
                if (!AuxiliarUtils.isNomeValido(novoNome)) {
                    System.out.println("\u001B[31mNome inválido! Use apenas letras (mín. 3). Mantendo o anterior.\u001B[0m");
                } else {
                    p.setNome(novoNome);
                }
            }

            String novoDoc = reader.readLine("Novo Documento [" + p.getDocumento() + "]: ").trim();
            if (!novoDoc.isEmpty()) p.setDocumento(novoDoc);

            passageiroRepo.atualizar(p);
            System.out.println("\u001B[32m✔ Dados atualizados com sucesso!\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO EDITAR]: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Veículos
    public void editarVeiculo(LineReader reader) {
        try {
            Long id = AuxiliarUtils.lerIdValido(reader, "ID do veículo: ");
            Veiculo v = veiculoRepo.buscarPorId(id);
            if (v == null) {
                System.out.println("\u001B[31mVeículo não encontrado.\u001B[0m");
                return;
            }

            String novoModelo = reader.readLine("Novo Modelo [" + v.getLabel() + "]: ").trim();
            if (!novoModelo.isEmpty()) v.setLabel(novoModelo.toUpperCase());

            String novaPlaca = reader.readLine("Nova Placa [" + v.getPlaca() + "]: ").trim().toUpperCase();
            if (!novaPlaca.isEmpty()) {
                if (!AuxiliarUtils.isPlacaValida(novaPlaca)) {
                    System.out.println("\u001B[31mPlaca inválida! Mantendo a anterior.\u001B[0m");
                } else {
                    v.setPlaca(novaPlaca);
                }
            }

            String novaCapacidadeStr = reader.readLine("Nova Capacidade [" + v.getCapacidade() + "]: ").trim();
            if (!novaCapacidadeStr.isEmpty()) {
                try {
                    int novaCapacidade = Integer.parseInt(novaCapacidadeStr);
                    if (novaCapacidade <= 0) {
                        System.out.println("\u001B[31mCapacidade inválida! Mantendo a anterior.\u001B[0m");
                    } else {
                        v.setCapacidade(novaCapacidade);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\u001B[31mValor não numérico! Mantendo a capacidade anterior.\u001B[0m");
                }
            }

            veiculoRepo.atualizar(v);
            System.out.println("\u001B[32m✔ Veículo atualizado!\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31mErro ao editar veículo: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Motoristas
    public void editarMotorista(LineReader reader) {
        System.out.println("\n\u001B[34m--- EDITAR MOTORISTA --- \u001B[0m");
        try {
            Integer idMotorista = AuxiliarUtils.lerIdValido(reader, "ID do Motorista para esta OS: ").intValue();
            Motorista m = motoristaRepo.buscarPorId(idMotorista);
            if (m == null) {
                System.out.println("\u001B[31m[ERRO]: motorista não encontrado.\u001B[0m");
                return;
            }

            String novoNome = reader.readLine("Novo Nome [" + m.getNome() + "]: ").trim();
            if (!novoNome.isEmpty()) {
                if (!AuxiliarUtils.isNomeValido(novoNome)) {
                    System.out.println("\u001B[31mNome inválido! Mantendo o anterior.\u001B[0m");
                } else {
                    m.setNome(novoNome.toUpperCase());
                }
            }

            String novaCnh = reader.readLine("Nova CNH [" + m.getCnh() + "]: ").trim();
            if (!novaCnh.isEmpty()) {
                if (novaCnh.length() != 11 || !AuxiliarUtils.isApenasNumeros(novaCnh)) {
                    System.out.println("\u001B[31mCNH inválida! Mantendo a anterior.\u001B[0m");
                } else {
                    m.setCnh(novaCnh);
                }
            }

            motoristaRepo.atualizar(m);
            System.out.println("\u001B[32m✔ Motorista atualizado com sucesso!\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: entrada inválida ou falha no banco.\u001B[0m");
        }
    }


}