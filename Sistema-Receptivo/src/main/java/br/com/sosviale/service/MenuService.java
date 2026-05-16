package br.com.sosviale.service;

import br.com.sosviale.repository.*;
import br.com.sosviale.util.*; // Importa as suas classes organizadas
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class MenuService {

    // 1. Repositórios injetados pelo construtor
    private final PassageiroRepository passageiroRepo;
    private final VeiculoRepository veiculoRepo;
    private final TransferRepository transferRepo;
    private final MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private final OrdemServicoRepository osRepo;

    // 2. Declaramos as classes utilitárias que vão fazer o trabalho pesado
    private final Cadastrar cadastrar;
    private final Listagens listagens;
    private final Editar editar;
    private final Excluir excluir;
    private final Agendamentos agendamentos;

    // Construtor que recebe os repositórios instanciados pela Main
    public MenuService(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo,
                       TransferRepository transferRepo, MotoristaRepository motoristaRepo,
                       PontoColetaRepository pontoColetaRepo, OrdemServicoRepository osRepo) {

        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
        this.motoristaRepo = motoristaRepo;
        this.pontoColetaRepo = pontoColetaRepo;
        this.osRepo = osRepo;

        // 3. Instanciamos as classes utilitárias passando os repositórios que elas precisam!
        this.cadastrar = new Cadastrar(passageiroRepo, veiculoRepo, transferRepo, motoristaRepo, pontoColetaRepo, osRepo);
        this.listagens = new Listagens(passageiroRepo, veiculoRepo, transferRepo, motoristaRepo, pontoColetaRepo, osRepo);
        this.editar = new Editar(passageiroRepo, veiculoRepo, transferRepo, motoristaRepo, pontoColetaRepo, osRepo);
        this.excluir = new Excluir(passageiroRepo, veiculoRepo, transferRepo, motoristaRepo, pontoColetaRepo, osRepo);
        this.agendamentos = new Agendamentos(passageiroRepo, veiculoRepo, transferRepo, motoristaRepo, pontoColetaRepo, osRepo);
    }

    // inicia e mantém o loop do menu principal até o usuário digitar "sair"
    public void menu() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

            boolean executando = true;
            String prompt = new AttributedStringBuilder()
                    .append("OPÇÃO", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold())
                    .append(" > ", AttributedStyle.DEFAULT)
                    .toAnsi();

            while (executando) {
                System.out.println("\u001B[36m========================================");
                System.out.println("   SISTEMA DE AGENDAMENTOS - SOS VIALE ");
                System.out.println("========================================\u001B[0m");
                System.out.println("\u001B[32m[1]\u001B[0m Transfers");
                System.out.println("\u001B[32m[2]\u001B[0m Passageiros");
                System.out.println("\u001B[32m[3]\u001B[0m Motoristas");
                System.out.println("\u001B[32m[4]\u001B[0m Veiculos");
                System.out.println("\u001B[32m[5]\u001B[0m Ordens de Serviço (FROTA)");
                System.out.println("\u001B[32m[sair]\u001B[0m Encerra o sistema");
                String comando = reader.readLine(prompt).toLowerCase().trim();

                switch (comando) {
                    case "1":
                        menuTransfer(reader);
                        break;
                    case "2":
                        menuPassageiro(reader);
                        break;
                    case "3":
                        menuMotorista(reader);
                        break;
                    case "4":
                        menuVeiculos(reader);
                        break;
                    case "5":
                        OrdemServicoMenu osMenu = new OrdemServicoMenu(passageiroRepo, veiculoRepo, transferRepo, motoristaRepo, pontoColetaRepo, osRepo);
                        osMenu.menuOrdemServico(reader);
                        break;
                    case "sair":
                        executando = false;
                        break;
                    default:
                        System.out.println("\u001B[31mComando desconhecido.\u001B[0m");
                }
            }
        } catch (Exception e) {
            System.err.println("erro na interface do terminal: " + e.getMessage());
        }
    }

    private void menuTransfer(LineReader reader) {
        System.out.println("\n\u001B[35m--- GESTÃO DE TRANSFERS --- \u001B[0m");
        System.out.println("\u001B[32m[1]\u001B[0m Cadastrar");
        System.out.println("\u001B[32m[2]\u001B[0m Listar");
        System.out.println("\u001B[32m[3]\u001B[0m Editar");
        System.out.println("\u001B[32m[4]\u001B[0m Excluir");
        System.out.println("\u001B[32m[5]\u001B[0m Voltar");

        String op = reader.readLine("Escolha uma opção: ").toLowerCase().trim();
        switch (op) {
            case "1": agendamentos.agendarTransfer(reader); break; // <-- Chamando o metodo pelo objeto!
            case "2": listagens.listarTransfers(); break;
            case "3": editar.editarTransfer(reader); break;
            case "4": excluir.excluirTransfer(reader); break;
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }

    private void menuPassageiro(LineReader reader) {
        System.out.println("\n\u001B[35m--- GESTÃO DE PASSAGEIROS --- \u001B[0m");
        System.out.println("\u001B[32m[1]\u001B[0m Cadastrar");
        System.out.println("\u001B[32m[2]\u001B[0m Listar");
        System.out.println("\u001B[32m[3]\u001B[0m Editar");
        System.out.println("\u001B[32m[4]\u001B[0m Excluir");
        System.out.println("\u001B[32m[5]\u001B[0m Voltar");
        String op = reader.readLine("Escolha uma opção: ").toLowerCase().trim();
        switch (op) {
            case "1": cadastrar.cadastrarPassageiro(reader); break;
            case "2": listagens.listarPassageiros(); break;
            case "3": editar.editarPassageiro(reader); break;
            case "4": excluir.excluirPassageiro(reader); break;
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }

    private void menuMotorista(LineReader reader) {
        System.out.println("\n\u001B[35m--- GESTÃO DE MOTORISTAS --- \u001B[0m");
        System.out.println("\u001B[32m[1]\u001B[0m Cadastrar");
        System.out.println("\u001B[32m[2]\u001B[0m Listar");
        System.out.println("\u001B[32m[3]\u001B[0m Editar");
        System.out.println("\u001B[32m[4]\u001B[0m Excluir");
        System.out.println("\u001B[32m[5]\u001B[0m Voltar");
        String op = reader.readLine("Escolha uma opção: ").toLowerCase().trim();
        switch (op) {
            case "1": cadastrar.cadastrarMotorista(reader); break;
            case "2": listagens.listarMotoristas(); break;
            case "3": editar.editarMotorista(reader); break;
            case "4": excluir.excluirMotorista(reader); break;
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }

    private void menuVeiculos(LineReader reader) {
        System.out.println("\n\u001B[35m--- GESTÃO DE VEICULOS --- \u001B[0m");
        System.out.println("\u001B[32m[1]\u001B[0m Cadastrar");
        System.out.println("\u001B[32m[2]\u001B[0m Listar");
        System.out.println("\u001B[32m[3]\u001B[0m Editar");
        System.out.println("\u001B[32m[4]\u001B[0m Excluir");
        System.out.println("\u001B[32m[5]\u001B[0m Voltar");
        String op = reader.readLine("Escolha uma opção: ").toLowerCase().trim();
        switch (op) {
            case "1": cadastrar.cadastrarVeiculo(reader); break;
            case "2": listagens.listarVeiculos(); break;
            case "3": editar.editarVeiculo(reader); break;
            case "4": excluir.excluirVeiculo(reader); break;
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }
}