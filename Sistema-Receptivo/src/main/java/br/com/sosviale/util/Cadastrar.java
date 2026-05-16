package br.com.sosviale.util;
import br.com.sosviale.model.*;
import br.com.sosviale.repository.*;
import org.jline.reader.LineReader;

import br.com.sosviale.util.AuxiliarUtils;
import br.com.sosviale.util.Agendamentos;
import br.com.sosviale.util.Editar;
import br.com.sosviale.util.Excluir;
import br.com.sosviale.util.Listagens;

public class Cadastrar {

    // repositórios injetados pelo construtor
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private OrdemServicoRepository osRepo;

    // construtor que recebe os repositórios instanciados pela Main
    public Cadastrar(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo,
                        TransferRepository transferRepo, MotoristaRepository motoristaRepo,
                        PontoColetaRepository pontoColetaRepo, OrdemServicoRepository osRepo) {
        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
        this.motoristaRepo = motoristaRepo;
        this.pontoColetaRepo = pontoColetaRepo;
        this.osRepo = osRepo;
    }

    // Passageiros
    public void cadastrarPassageiro(LineReader reader) {
        String opcao;
        do {
            System.out.println("\n\u001B[36m--- CADASTRO DE PASSAGEIRO --- \u001B[0m");
            try {
                String nome = AuxiliarUtils.lerNomeValido(reader, "Nome Completo: ");
                String documento = reader.readLine("Documento (RG/Passaporte): ").trim();
                String nacionalidade = AuxiliarUtils.lerStringObrigatoria(reader, "Nacionalidade: ");

                // aviso: passageiro sem documento não pode realizar transfers internacionais
                if (documento.isEmpty()) {
                    System.out.println("\u001B[33m[AVISO]: sem documento, este passageiro não poderá realizar transfers internacionais.\u001B[0m");
                }

                Passageiro p = new Passageiro(nome, documento, nacionalidade);
                passageiroRepo.salvar(p);
                System.out.println("\u001B[32m✔ Passageiro cadastrado com sucesso!\u001B[0m");

            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
            }

            System.out.println("\u001B[32m[1]\u001B[0m Retornar ao Menu Inicial");
            System.out.println("\u001B[32m[2]\u001B[0m Cadastrar novo passageiro");
            opcao = reader.readLine("Escolha: ").trim();
            if (opcao.equals("1")) return;
            if (!opcao.equals("2")) {
                System.out.println("\u001B[31mOpção inválida.\u001B[0m");
                opcao = "1"; // encerra o loop em caso de entrada inválida
            }
        } while (opcao.equals("2"));
    }

    // Motorista
    public void cadastrarMotorista(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE MOTORISTA --- \u001B[0m");
        try {
            String nome = AuxiliarUtils.lerNomeValido(reader, "Nome Completo: ").toUpperCase().trim();

            // CNH deve ter exatamente 11 dígitos numéricos
            String cnh = "";
            while (true) {
                cnh = reader.readLine("Número da CNH (11 dígitos): ").trim();
                if (cnh.length() == 11 && AuxiliarUtils.isApenasNumeros(cnh)) break;
                System.out.println("\u001B[31mCNH inválida! Deve conter exatamente 11 números.\u001B[0m");
            }

            Motorista m = new Motorista();
            m.setNome(nome);
            m.setCnh(cnh);
            motoristaRepo.salvar(m);
            System.out.println("\u001B[32m✔ Motorista " + nome + " salvo com sucesso!\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    // Veículo
    public void cadastrarVeiculo(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE VEÍCULO --- \u001B[0m");
        try {
            String label = AuxiliarUtils.lerStringObrigatoria(reader, "Modelo (Ex: Mercedes Sprinter): ").toUpperCase().trim();

            // placa no padrão Mercosul (ABC1D23)
            String placa = "";
            while (true) {
                placa = reader.readLine("Placa (padrão Mercosul, ex: ABC1D23): ").toUpperCase().trim();
                if (AuxiliarUtils.isPlacaValida(placa)) break;
                System.out.println("\u001B[31mPlaca inválida! Use o padrão Mercosul (ABC1D23).\u001B[0m");
            }

            int capacidade = 0;
            while (true) {
                String capStr = reader.readLine("Capacidade de passageiros: ").trim();
                if (AuxiliarUtils.isApenasNumeros(capStr) && Integer.parseInt(capStr) > 0) {
                    capacidade = Integer.parseInt(capStr);
                    break;
                }
                System.out.println("\u001B[31mCapacidade deve ser um número maior que zero.\u001B[0m");
            }

            Veiculo v = new Veiculo();
            v.setLabel(label);
            v.setPlaca(placa);
            v.setCapacidade(capacidade);
            veiculoRepo.salvar(v);
            System.out.println("\u001B[32m✔ Veículo " + label + " (" + placa + ") cadastrado!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
        }
    }

}
