package br.com.sosviale.service;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.repository.TransferRepository;
import br.com.sosviale.repository.VeiculoRepository;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MenuService {

    // atributos para guardar os repositórios
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;

    // construtor que recebe os repositórios da main
    public MenuService(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo, TransferRepository transferRepo, MotoristaRepository motoristaRepo) {
        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
        this.motoristaRepo = motoristaRepo;
    }

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
                System.out.println("\u001B[32m[1]\u001B[0m Agendar Transfer");
                System.out.println("\u001B[32m[2]\u001B[0m Cadastrar Passageiro");
                System.out.println("\u001B[32m[3]\u001B[0m Listar Passageiros");
                System.out.println("\u001B[32m[4]\u001B[0m Cadastrar Motorista");
                System.out.println("\u001B[32m[5]\u001B[0m Listar Motorista");
                System.out.println("\u001B[32m[sair]\u001B[0m Encerra o sistema");
                String comando = reader.readLine(prompt).toLowerCase().trim();

                switch (comando) {
                    case "1":
                        agendarTransfer(reader);
                        break;
                    case "2":
                        cadastrarPassageiro(reader);
                        break;
                    case "3":
                        listarPassageiros();
                        break;
                    case "4":
                        cadastrarMotorista(reader);
                        break;
                    case "5":
                        listarMotoristas();
                        break;
                    case "sair":
                        executando = false;
                        break;
                    default:
                        System.out.println("\u001B[31mComando desconhecido.\u001B[0m");
                }


            }
        } catch (Exception e) {
            System.err.println("Erro na interface: " + e.getMessage());
        }
    }

    private void agendarTransfer(LineReader reader) {
        System.out.println("\n\u001B[36m--- NOVO AGENDAMENTO DE TRANSFER --- \u001B[0m");

        try {
            // coleta de dados básicos
            String origem = reader.readLine("Origem (Ex: Aeroporto IGU): ");
            String destino = reader.readLine("Destino (Ex: Hotel Cataratas): ");
            String valorStr = reader.readLine("Valor do Transfer (R$): ");
            BigDecimal valor = new BigDecimal(valorStr.replace(",", "."));

            // para facilitar a vida do usuário
            String ver = reader.readLine("Deseja listar passageiros e veículos antes de informar os IDs? (s/n): ");
            if (ver.equalsIgnoreCase("s")) {
                listarPassageiros();
                listarMotoristas();
                // adicionar listarVeiculos()
            }

            String passageiroId = reader.readLine("\nDigite o ID do Passageiro: ");
            String motoristaId = reader.readLine("Digite o ID do Motorista: ");
            String veiculoId = reader.readLine("Digite o ID do Veículo: ");


            // busca os objetos reais no banco
            Passageiro passageiro = passageiroRepo.buscarPorId(Long.parseLong(passageiroId));
            Motorista motorista = motoristaRepo.buscarPorId(Long.parseLong(motoristaId));
            Veiculo veiculo = veiculoRepo.buscarPorId(Long.parseLong(veiculoId));

            //da pra melhorar essa verificação, vou deixar essa temporariamente
            if (passageiro == null || veiculo == null || motorista == null) {
                throw new Exception("Passageiro, Veículo ou Motorista não encontrado!");
            }
            //----------------------------------------

            // criação do Objeto Transfer
            Transfer novoTransfer = new Transfer();
            novoTransfer.setOrigem(origem);
            novoTransfer.setDestino(destino);
            novoTransfer.setValorBase(valor);
            novoTransfer.setDataHora(LocalDateTime.now().plusDays(1)); // Ex: Amanhã

            novoTransfer.setMotorista(motorista);
            novoTransfer.setVeiculo(veiculo);

            // relacionamento ManyToMany (adicionando o passageiro à lista)
            novoTransfer.getPassageiros().add(passageiro);

            // salva no banco via repository
            transferRepo.salvar(novoTransfer);

            System.out.println("\n\u001B[32m✔ Transfer agendado com sucesso para " + passageiro.getNome() + "!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO NO AGENDAMENTO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void cadastrarPassageiro(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE PASSAGEIRO --- \u001B[0m");

        try {
            // lendo os dados usando o reader da JLine
            String nome = reader.readLine("Nome Completo: ");
            if (nome.trim().isEmpty()) throw new Exception("O nome não pode ser vazio.");

            String documento = reader.readLine("Documento (RG/Passaporte): ");
            String nacionalidade = reader.readLine("Nacionalidade: ");

            // (regra de negócio) validação de fronteira
            // se for para a Argentina/Paraguai, o documento é obrigatório
            if (documento.trim().isEmpty()) {
                System.out.println("\u001B[33m[AVISO]: Sem documento, este passageiro não poderá realizar transfers internacionais.\u001B[0m");
            }

            System.out.println("\n\u001B[32mSalvando no banco de dados...\u001B[0m");

            Passageiro p = new Passageiro(nome, documento, nacionalidade);
            passageiroRepo.salvar(p);

            System.out.println("\u001B[32m✔ Passageiro cadastrado com sucesso!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void listarPassageiros() {
        System.out.println("\n\u001B[36m--- LISTA DE PASSAGEIROS CADASTRADOS --- \u001B[0m");

        try {
            // Busca a lista real do banco via Repository
            List<Passageiro> lista = passageiroRepo.listarTodos(); // Verifique se o nome no seu repo é buscarTodos ou listar

            if (lista.isEmpty()) {
                System.out.println("\u001B[33mNenhum passageiro encontrado.\u001B[0m");
                return;
            }

            // Cabeçalho da "Tabela"
            System.out.println(String.format("%-5s | %-25s | %-15s | %-15s", "ID", "NOME", "DOCUMENTO", "NACIONALIDADE"));
            System.out.println("-------------------------------------------------------------------------");

            // Linhas da Tabela
            for (Passageiro p : lista) {
                System.out.println(String.format("%-5d | %-25s | %-15s | %-15s",
                        p.getId(),
                        p.getNome(),
                        p.getDocumento(),
                        p.getNacionalidade()));
            }
            System.out.println("-------------------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO LISTAR]: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void cadastrarMotorista(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE MOTORISTA --- \u001B[0m");
        try {
            String nome = reader.readLine("Nome Completo: ").toUpperCase().trim();
            String cnh = reader.readLine("Número da CNH (11 dígitos): ").trim();
            String categoria = reader.readLine("Categoria (A, B, D, etc): ").toUpperCase().trim();

            // validação da CNH
            if (cnh.length() != 11) {
                throw new Exception("CNH Inválida! O número deve conter exatamente 11 dígitos.");
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

    private void listarMotoristas() {
        System.out.println("\n\u001B[36m--- MOTORISTAS CADASTRADOS --- \u001B[0m");
        List<Motorista> lista = motoristaRepo.listarTodos();
        System.out.println(String.format("%-5s | %-25s | %-15s", "ID", "NOME", "CNH"));
        for (Motorista m : lista) {
            System.out.println(String.format("%-5d | %-25s | %-15s", m.getId(), m.getNome(), m.getCnh()));
        }
    }
}
