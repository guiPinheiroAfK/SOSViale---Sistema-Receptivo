package br.com.sosviale.service;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.model.PontoColeta;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.repository.TransferRepository;
import br.com.sosviale.repository.VeiculoRepository;
import br.com.sosviale.repository.PontoColetaRepository;
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
                System.out.println("\u001B[32m[1]\u001B[0m Transfers");
                System.out.println("\u001B[32m[2]\u001B[0m Passageiros");
                System.out.println("\u001B[32m[3]\u001B[0m Motoristas");
                System.out.println("\u001B[32m[4]\u001B[0m Veiculos");
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
            // 1. Dados básicos da viagem
            String origem = reader.readLine("Origem (Ex: Aeroporto IGU): ");
            String destino = reader.readLine("Destino (Ex: Hotel Cataratas): ");
            String valorStr = reader.readLine("Valor Total do Transfer (R$): ");
            BigDecimal valor = new BigDecimal(valorStr.replace(",", "."));

            // Apoio ao usuário
            String ver = reader.readLine("Deseja listar Motoristas e Veículos? (s/n): ");
            if (ver.equalsIgnoreCase("s")) {
                listarMotoristas();
                listarVeiculos();
            }

            // 2. Definir quem leva e em qual carro
            String motoristaId = reader.readLine("\nDigite o ID do Motorista: ");
            String veiculoId = reader.readLine("Digite o ID do Veículo: ");

            Motorista motorista = motoristaRepo.buscarPorId(Long.parseLong(motoristaId));
            Veiculo veiculo = veiculoRepo.buscarPorId(Long.parseLong(veiculoId));

            if (motorista == null || veiculo == null) {
                throw new Exception("Motorista ou Veículo não encontrado!");
            }

            // 3. Criar o objeto Transfer antes do loop de passageiros
            Transfer novoTransfer = new Transfer();
            novoTransfer.setOrigem(origem);
            novoTransfer.setDestino(destino);
            novoTransfer.setValorBase(valor);
            novoTransfer.setDataHora(LocalDateTime.now().plusDays(1));
            novoTransfer.setMotorista(motorista);
            novoTransfer.setVeiculo(veiculo);

            String ver2 = reader.readLine("Deseja listar Passageiros? (s/n): ");
            if (ver2.equalsIgnoreCase("s")) {
                listarPassageiros();
            }

            // 4. LOOP DE PASSAGEIROS (O CORAÇÃO DA MUDANÇA)
            System.out.println("\n\u001B[33m--- ADICIONANDO PASSAGEIROS (Capacidade: " + veiculo.getCapacidade() + ") --- \u001B[0m");

            boolean adicionando = true;
            while (adicionando) {
                int atuais = novoTransfer.getPassageiros().size();

                if (atuais >= veiculo.getCapacidade()) {
                    System.out.println("\u001B[31mLOTAÇÃO MÁXIMA ATINGIDA!\u001B[0m");
                    break;
                }

                System.out.println("Passageiros atuais: " + atuais + "/" + veiculo.getCapacidade());
                String pIdStr = reader.readLine("Digite o ID do Passageiro (ou 'fim' para encerrar): ");

                if (pIdStr.equalsIgnoreCase("fim")) {
                    if (atuais == 0) {
                        System.out.println("\u001B[31mErro: O transfer precisa de pelo menos 1 passageiro!\u001B[0m");
                        continue;
                    }
                    adicionando = false;
                } else {
                    Passageiro p = passageiroRepo.buscarPorId(Long.parseLong(pIdStr));
                    if (p == null) {
                        System.out.println("\u001B[31mPassageiro não encontrado!\u001B[0m");
                    } else if (novoTransfer.getPassageiros().contains(p)) {
                        System.out.println("\u001B[31mEste passageiro já está na lista!\u001B[0m");
                    } else {
                        novoTransfer.getPassageiros().add(p);
                        System.out.println("\u001B[32m✔ " + p.getNome() + " adicionado.\u001B[0m");
                    }
                }
            }

            // 5. Salva tudo de uma vez
            transferRepo.salvar(novoTransfer);
            System.out.println("\n\u001B[32m✔ Transfer agendado com sucesso com " + novoTransfer.getPassageiros().size() + " passageiro(s)!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO NO AGENDAMENTO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void listarTransfers() {
        System.out.println("\n\u001B[36m--- LISTA DE TRANSFERS AGENDADOS --- \u001B[0m");
        try {
            List<Transfer> lista = transferRepo.listarTodos();
            if (lista.isEmpty()) {
                System.out.println("Nenhum agendamento encontrado.");
                return;
            }

            // Ajustei o espaçamento para caber a coluna de passageiros
            System.out.println(String.format("%-4s | %-15s | %-15s | %-15s | %-30s | %-10s",
                    "ID", "ORIGEM", "DESTINO", "MOTORISTA", "PASSAGEIROS", "VALOR"));
            System.out.println("------------------------------------------------------------------------------------------------------------------------");

            for (Transfer t : lista) {
                // Transforma a lista de objetos Passageiro em uma String de nomes separada por vírgula
                String nomesPassageiros = t.getPassageiros().stream()
                        .map(Passageiro::getNome)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Nenhum");

                System.out.println(String.format("%-4d | %-15s | %-15s | %-15s | %-30s | R$%-10.2f",
                        t.getId(),
                        t.getOrigem(),
                        t.getDestino(),
                        t.getMotorista().getNome(),
                        nomesPassageiros,
                        t.getValorBase()));
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro ao listar: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void editarTransfer(LineReader reader) {
        try {
            String idStr = reader.readLine("ID do Transfer para editar: ");
            Transfer t = transferRepo.buscarPorId(Long.parseLong(idStr));

            if (t != null) {
                String novaOrigem = reader.readLine("Nova Origem [" + t.getOrigem() + "]: ");
                if (!novaOrigem.trim().isEmpty()) t.setOrigem(novaOrigem);

                String novoDestino = reader.readLine("Novo Destino [" + t.getDestino() + "]: ");
                if (!novoDestino.trim().isEmpty()) t.setDestino(novoDestino);

                String novoValor = reader.readLine("Novo Valor [R$ " + t.getValorBase() + "]: ");
                if (!novoValor.trim().isEmpty()) t.setValorBase(new BigDecimal(novoValor.replace(",", ".")));

                transferRepo.atualizar(t);
                System.out.println("\u001B[32m✔ Transfer atualizado com sucesso!\u001B[0m");
            } else {
                System.out.println("Agendamento não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro na edição.\u001B[0m");
        }
    }

    private void excluirTransfer(LineReader reader) {
        try {
            String idStr = reader.readLine("ID do Transfer para cancelar: ");
            Long id = Long.parseLong(idStr);

            String conf = reader.readLine("Confirmar cancelamento? (s/n): ");
            if (conf.equalsIgnoreCase("s")) {
                transferRepo.excluir(id);
                System.out.println("\u001B[32m✔ Agendamento cancelado.\u001B[0m");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro ao cancelar.\u001B[0m");
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
            case "1": agendarTransfer(reader); break;
            case "2": listarTransfers(); break;
            case "3": editarTransfer(reader); break;
            case "4": excluirTransfer(reader); break;
            case "5": return; // Sai do metodo e volta para o menu principal
            default: System.out.println("Opção inválida.");
        }
    }


    private void cadastrarPassageiro(LineReader reader) {
        String opcao;
        do {
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
            System.out.println("\u001B[32m[1]\u001B[0m Retornar ao Menu Inicial");
            System.out.println("\u001B[32m[2]\u001B[0m Cadastrar novo passageiro");
            opcao = reader.readLine("Escolha: ").trim();
            if (opcao.equals("1")) {
                menu();
            } else if (!opcao.equals("1") && !opcao.equals("2")) {
                System.out.println("\u001B[31mOpção inválida.\u001B[0m");
                System.out.println("\u001B[32m[1]\u001B[0m Retornar ao Menu Inicial");
                System.out.println("\u001B[32m[2]\u001B[0m Cadastrar novo passageiro");
                opcao = reader.readLine("Escolha: ").trim();
            }

        }while (opcao.equalsIgnoreCase("2")) ;
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

    private void excluirPassageiro(LineReader reader) {
        System.out.println("\n\u001B[31m--- EXCLUIR PASSAGEIRO --- \u001B[0m");
        try {
            // 1. Peça o ID
            String idStr = reader.readLine("Digite o ID do passageiro para excluir: ");
            Long id = Long.parseLong(idStr);

            // 2. Busque o passageiro para mostrar o nome dele na confirmação
            Passageiro p = passageiroRepo.buscarPorId(id);
            if (p == null) {
                System.out.println("Passageiro não encontrado!");
                return;
            }

            // 3. Peça confirmação (Segurança em primeiro lugar!)
            String conf = reader.readLine("Tem certeza que deseja excluir " + p.getNome() + "? (s/n): ");
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

    private void editarPassageiro(LineReader reader) {
        System.out.println("\n\u001B[34m--- EDITAR PASSAGEIRO --- \u001B[0m");
        try {
            // 1. Busque quem vai ser editado
            String idStr = reader.readLine("ID do passageiro: ");
            Passageiro p = passageiroRepo.buscarPorId(Long.parseLong(idStr));

            if (p != null) {
                // 2. Peça os novos dados (se der Enter sem digitar, mantém o antigo)
                String novoNome = reader.readLine("Novo Nome [" + p.getNome() + "]: ");
                if (!novoNome.trim().isEmpty()) p.setNome(novoNome);

                String novoDoc = reader.readLine("Novo Documento [" + p.getDocumento() + "]: ");
                if (!novoDoc.trim().isEmpty()) p.setDocumento(novoDoc);

                // 3. Mande pro Repository fazer o 'merge'
                passageiroRepo.atualizar(p);
                System.out.println("\u001B[32m✔ Dados atualizados com sucesso!\u001B[0m");
            } else {
                System.out.println("Passageiro não encontrado.");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO EDITAR]: " + e.getMessage() + "\u001B[0m");
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
            case "1": cadastrarPassageiro(reader); break;
            case "2": listarPassageiros(); break;
            case "3": editarPassageiro(reader); break;
            case "4": excluirPassageiro(reader); break;
            case "5": return; // Sai do metodo e volta para o menu principal
            default: System.out.println("Opção inválida.");
        }
    }

    private void cadastrarMotorista(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE MOTORISTA --- \u001B[0m");
        try {
            String nome = reader.readLine("Nome Completo: ").toUpperCase().trim();
            String cnh = reader.readLine("Número da CNH (11 dígitos): ").trim();

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

    private void editarMotorista(LineReader reader) {
        System.out.println("\n\u001B[34m--- EDITAR MOTORISTA --- \u001B[0m");
        try {
            String idStr = reader.readLine("Digite o ID do motorista: ");
            Motorista m = motoristaRepo.buscarPorId(Long.parseLong(idStr));

            if (m != null) {
                String novoNome = reader.readLine("Novo Nome [" + m.getNome() + "]: ");
                if (!novoNome.trim().isEmpty()) m.setNome(novoNome.toUpperCase());

                String novaCnh = reader.readLine("Nova CNH [" + m.getCnh() + "]: ");
                if (!novaCnh.trim().isEmpty()) m.setCnh(novaCnh);

                motoristaRepo.atualizar(m);
                System.out.println("\u001B[32m✔ Motorista atualizado com sucesso!\u001B[0m");
            } else {
                System.out.println("\u001B[31m[ERRO]: Motorista não encontrado.\u001B[0m");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: Entrada inválida ou falha no banco.\u001B[0m");
        }
    }

    private void excluirMotorista(LineReader reader) {
        System.out.println("\n\u001B[31m--- EXCLUIR MOTORISTA --- \u001B[0m");
        try {
            String idStr = reader.readLine("ID para excluir: ");
            Long id = Long.parseLong(idStr);

            Motorista m = motoristaRepo.buscarPorId(id);
            if (m == null) {
                System.out.println("Motorista não encontrado.");
                return;
            }

            String conf = reader.readLine("Excluir " + m.getNome() + "? (s/n): ");
            if (conf.equalsIgnoreCase("s")) {
                motoristaRepo.excluir(id);
                System.out.println("\u001B[32m✔ Motorista removido!\u001B[0m");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: Não foi possível excluir (pode estar vinculado a um transfer).\u001B[0m");
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
            case "1": cadastrarMotorista(reader); break;
            case "2": listarMotoristas(); break;
            case "3": editarMotorista(reader); break;
            case "4": excluirMotorista(reader); break;
            case "5": return; // Sai do metodo e volta para o menu principal
            default: System.out.println("Opção inválida.");
        }
    }

    private void cadastrarVeiculo(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE VEÍCULO --- \u001B[0m");
        try {
            String label = reader.readLine("Modelo (Ex: Mercedes Sprinter): ").toUpperCase().trim();
            String placa = reader.readLine("Placa: ").toUpperCase().trim();
            int capacidade = Integer.parseInt(reader.readLine("Capacidade de Passageiros: "));

            // validação simples
            if (capacidade <= 0) {
                throw new Exception("A capacidade deve ser maior que zero.");
            }

            Veiculo v = new Veiculo();
            v.setLabel(label);
            v.setPlaca(placa);
            v.setCapacidade(capacidade);

            veiculoRepo.salvar(v);

            System.out.println("\u001B[32m✔ Veículo " + label + " (" + placa + ") cadastrado!\u001B[0m");

        } catch (NumberFormatException e) {
            System.out.println("\u001B[31m[ERRO]: A capacidade deve ser um número inteiro.\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void listarVeiculos() {
        System.out.println("\n\u001B[36m--- FROTA DE VEÍCULOS --- \u001B[0m");
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
    }

    private void editarVeiculo(LineReader reader) {
        try {
            String idStr = reader.readLine("ID do veículo: ");
            Veiculo v = veiculoRepo.buscarPorId(Long.parseLong(idStr));

            if (v != null) {
                String novoModelo = reader.readLine("Novo Modelo [" + v.getLabel() + "]: ");
                if (!novoModelo.trim().isEmpty()) v.setLabel(novoModelo);

                String novaPlaca = reader.readLine("Nova Placa [" + v.getPlaca() + "]: ");
                if (!novaPlaca.trim().isEmpty()) v.setPlaca(novaPlaca);

                String novaCapacidadeStr = reader.readLine("Nova Capacidade [" + v.getCapacidade() + "]: ");
                if (!novaCapacidadeStr.trim().isEmpty()) {
                    int novaCapacidade = Integer.parseInt(novaCapacidadeStr);
                    if (novaCapacidade <= 0) {
                        System.out.println("\u001B[31mCapacidade inválida! Mantendo a anterior.\u001B[0m");
                    } else {
                        v.setCapacidade(novaCapacidade);
                    }
                }

                veiculoRepo.atualizar(v);
                System.out.println("\u001B[32m✔ Veículo atualizado!\u001B[0m");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro ao editar veículo.\u001B[0m");
        }
    }

    private void excluirVeiculo(LineReader reader) {
        try {
            String idStr = reader.readLine("ID para excluir: ");
            Long id = Long.parseLong(idStr);

            String conf = reader.readLine("Tem certeza? (s/n): ");
            if (conf.equalsIgnoreCase("s")) {
                veiculoRepo.excluir(id);
                System.out.println("\u001B[32m✔ Veículo removido!\u001B[0m");
            }
        } catch (Exception e) {
            System.out.println("\u001B[31mErro: Veículo pode estar em uso num Transfer.\u001B[0m");
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
                    case "1": cadastrarVeiculo(reader); break;
                    case "2": listarVeiculos(); break;
                    case "3": editarVeiculo(reader); break;
                    case "4": excluirVeiculo(reader); break;
                    case "5": return; // Sai do metodo e volta para o menu principal
                    default: System.out.println("Opção inválida.");
                }
            }
}
