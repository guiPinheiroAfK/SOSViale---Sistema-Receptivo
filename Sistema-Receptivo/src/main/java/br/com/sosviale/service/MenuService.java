package br.com.sosviale.service;

import br.com.sosviale.repository.*;
import br.com.sosviale.service.StatusTransfer;
import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.model.PontoColeta;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class MenuService {

    // atributos para guardar os repositórios
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private OrdemServicoRepository osRepo;

    // construtor que recebe os repositórios da main
    public MenuService(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo, TransferRepository transferRepo, MotoristaRepository motoristaRepo, PontoColetaRepository pontoColetaRepo,
                       OrdemServicoRepository osRepo) {
        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
        this.motoristaRepo = motoristaRepo;
        this.pontoColetaRepo = pontoColetaRepo;
        this.osRepo = osRepo;
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
                System.out.println("\u001B[32m[5]\u001B[0m Ordens de Serviço (FROTA)"); // <-- A OPÇÃO 5 APARECE AQUI!
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
                    case "5": // <-- O CASE 5 REDIRECIONA PARA O MENU NOVO!
                        OrdemServicoMenu osMenu = new OrdemServicoMenu(osRepo, motoristaRepo, veiculoRepo, transferRepo);
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
            System.err.println("Erro na interface: " + e.getMessage());
        }
    }

    private void agendarTransfer(LineReader reader) {
        System.out.println("\n\u001B[36m--- NOVO AGENDAMENTO DE TRANSFER --- \u001B[0m");

        try {
            // dados básicos
            String origem = lerStringObrigatoria(reader, "Origem (Ex: Aeroporto IGU): ");
            String destino = lerStringObrigatoria(reader, "Destino (Ex: Hotel Cataratas): ");
            BigDecimal valor = lerBigDecimalValido(reader, "Valor Total do Transfer (R$): ");

            // Lendo a data e horário exato do transfer
            LocalDateTime dataHora = lerDataHoraValida(reader, "Data e Hora (dd/MM/yyyy HH:mm): ");

            // O Transfer agora nasce órfão de motorista e veículo
            Transfer novoTransfer = new Transfer(dataHora, origem, destino, valor);

            // Forçamos o status para AGENDADO por padrão ao criar
            novoTransfer.setStatus(StatusTransfer.AGENDADO);

            // LOOP DE PASSAGEIROS
            String ver2 = reader.readLine("Deseja listar Passageiros? (s/n): ");
            if (ver2.equalsIgnoreCase("s")) {
                listarPassageiros();
            }

            System.out.println("\n\u001B[33m--- ADICIONANDO PASSAGEIROS --- \u001B[0m");
            System.out.println("A capacidade será validada depois, ao atribuir o veículo na OS.");

            boolean adicionando = true;
            while (adicionando) {
                String pIdStr = reader.readLine("ID do Passageiro (ou 'fim'): ");
                if (pIdStr.equalsIgnoreCase("fim")) {
                    if (novoTransfer.getPassageiros().isEmpty()) {
                        System.out.println("\u001B[31mErro: Precisa de pelo menos 1 passageiro!\u001B[0m");
                        continue;
                    }
                    adicionando = false;
                } else {
                    Passageiro p = passageiroRepo.buscarPorId(Long.parseLong(pIdStr));
                    if (p != null && !novoTransfer.getPassageiros().contains(p)) {
                        novoTransfer.getPassageiros().add(p);
                        System.out.println("\u001B[32m✔ " + p.getNome() + " adicionado.\u001B[0m");
                    } else {
                        System.out.println("\u001B[31mPassageiro inválido ou já adicionado!\u001B[0m");
                    }
                }
            }

            // Salvar
            transferRepo.salvar(novoTransfer);
            System.out.println("\n\u001B[32m✔ Transfer [" + novoTransfer.getStatus() + "] salvo com sucesso e aguardando Ordem de Serviço!\u001B[0m");

            // Pontos de coleta continuam iguais...
            System.out.println("\n--- PONTOS DE COLETA (manual) ---");
            System.out.println("Digite 'fim' para encerrar.");
            int ordem = 1;
            boolean adicionandoPontos = true;
            while (adicionandoPontos) {
                String local = reader.readLine("Parada " + ordem + " - Local: ");
                if (local.equalsIgnoreCase("fim")) break;
                String horario = reader.readLine("Horário previsto (HH:mm, ou Enter pra pular): ");

                PontoColeta ponto = new PontoColeta();
                ponto.setTransfer(novoTransfer);
                ponto.setLocalColeta(local);
                ponto.setOrdemParada(ordem++);
                if (!horario.isBlank()) {
                    ponto.setHorarioPrevisto(LocalTime.parse(horario));
                }
                ponto.setLatitude(0.0);
                ponto.setLongitude(0.0);
                pontoColetaRepo.salvar(ponto);
            }

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    private void listarTransfers() {
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

                // Verifica se o transfer já tem uma OS e um motorista
                String situacaoOs = (t.getOrdemServico() != null && t.getOrdemServico().getMotorista() != null)
                        ? "OS #" + t.getOrdemServico().getId() + " - " + t.getOrdemServico().getMotorista().getNome()
                        : "Não Atribuído";

                System.out.println(String.format("%-4d | %-12s | %-15s | %-15s | %-20s | %-25s | R$%-10.2f",
                        t.getId(),
                        t.getStatus(),
                        t.getOrigem(),
                        t.getDestino(),
                        situacaoOs,
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

            if (t == null) {
                System.out.println("\u001B[31mAgendamento não encontrado.\u001B[0m");
                return;
            }

            System.out.println("\n\u001B[33m(Aperte ENTER para manter o valor atual)\u001B[0m");

            // editar origem
            String novaOrigem = reader.readLine("Nova Origem [" + t.getOrigem() + "]: ");
            if (!novaOrigem.trim().isEmpty()) t.setOrigem(novaOrigem);

            // editar destino
            String novoDestino = reader.readLine("Novo Destino [" + t.getDestino() + "]: ");
            if (!novoDestino.trim().isEmpty()) t.setDestino(novoDestino);

            // editar valor
            String novoValor = reader.readLine("Novo Valor [R$ " + t.getValorBase() + "]: ");
            if (!novoValor.trim().isEmpty()) t.setValorBase(new BigDecimal(novoValor.replace(",", ".")));

            // MUDAR O STATUS
            System.out.println("\nStatus atual: " + t.getStatus());
            System.out.println("Escolha o novo status:");
            System.out.println("1. AGENDADO");
            System.out.println("2. EM_ANDAMENTO");
            System.out.println("3. CONCLUIDO");
            System.out.println("4. CANCELADO");
            System.out.println("Enter para manter o mesmo.");

            String opStatus = reader.readLine("> ");
            switch (opStatus) {
                case "1" -> t.setStatus(StatusTransfer.AGENDADO);
                case "2" -> t.setStatus(StatusTransfer.EM_ANDAMENTO);
                case "3" -> t.setStatus(StatusTransfer.CONCLUIDO);
                case "4" -> t.setStatus(StatusTransfer.CANCELADO);
            }

            // salvar no banco
            transferRepo.atualizar(t);
            System.out.println("\n\u001B[32m✔ Transfer #" + t.getId() + " atualizado e purificado!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO NA EDIÇÃO]: " + e.getMessage() + "\u001B[0m");
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
                String nome = lerNomeValido(reader,"Nome Completo: ");
                if (!isNomeValido(nome)) {
                    throw new Exception("Nome inválido! Use pelo menos 3 letras e sem números.");
                }


                String documento = reader.readLine("Documento (RG/Passaporte): ");
                String nacionalidade = lerStringObrigatoria(reader,"Nacionalidade: ");

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
            // buscando quem vai ser editado
            String idStr = reader.readLine("ID do passageiro: ");
            Passageiro p = passageiroRepo.buscarPorId(Long.parseLong(idStr));

            if (p != null) {
                // pedindo os novos dados (se der Enter sem digitar, mantém o antigo)
                String novoNome = reader.readLine("Novo Nome [" + p.getNome() + "]: ");
                if (!novoNome.trim().isEmpty()) p.setNome(novoNome);

                String novoDoc = reader.readLine("Novo Documento [" + p.getDocumento() + "]: ");
                if (!novoDoc.trim().isEmpty()) p.setDocumento(novoDoc);

                // manda pro Repository fazer o 'merge'
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
            String nome = lerNomeValido(reader,"Nome Completo: ").toUpperCase().trim();
            String cnh = "";
            while (true) {
                cnh = reader.readLine("Número da CNH (11 dígitos): ").trim();
                if (cnh.length() == 11 && isApenasNumeros(cnh)) break;
                System.out.println("\u001B[31mCNH Inválida! Deve conter exatamente 11 números.\u001B[0m");
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
            String label = lerStringObrigatoria(reader,"Modelo (Ex: Mercedes Sprinter): ").toUpperCase().trim();
            String placa = "";
            while (true) {
                placa = reader.readLine("Placa: ").toUpperCase().trim();
                if (isPlacaValida(placa)) break;
                System.out.println("\u001B[31mPlaca inválida! Use o padrão Mercosul (ABC1D23).\u001B[0m");
            }
            int capacidade = 0;
            while (true) {
                String capStr = reader.readLine("Capacidade: ");
                if (isApenasNumeros(capStr) && Integer.parseInt(capStr) > 0) {
                    capacidade = Integer.parseInt(capStr);
                    break;
                }
                System.out.println("\u001B[31mCapacidade deve ser um número maior que zero.\u001B[0m");
            }

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

    // valida se é apenas letras e espaços (útil para nomes)
    public static boolean isNomeValido(String nome) {
        return nome != null && nome.trim().matches("^[a-zA-ZÀ-ÿ\\s]{3,}$");
    }

    // valida se a string contém apenas números
    public static boolean isApenasNumeros(String dados) {
        return dados != null && dados.matches("\\d+");
    }

    // valida placa (Padrão Antigo e Mercosul)
    public static boolean isPlacaValida(String placa) {
        return placa != null && placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}");
    }

    private String lerStringObrigatoria(LineReader reader, String mensagem) {
        while (true) {
            String valor = reader.readLine(mensagem).trim();
            if (!valor.isEmpty()) return valor;
            System.out.println("\u001B[31mEste campo é obrigatório!\u001B[0m");
        }
    }

    private String lerNomeValido(LineReader reader, String mensagem) {
        while (true) {
            String nome = reader.readLine(mensagem).trim();
            if (isNomeValido(nome)) return nome;
            System.out.println("\u001B[31mNome inválido! Use apenas letras (mín. 3).\u001B[0m");
        }
    }

    private Long lerIdValido(LineReader reader, String mensagem) {
        while (true) {
            String idStr = reader.readLine(mensagem).trim();
            if (isApenasNumeros(idStr)) return Long.parseLong(idStr);
            System.out.println("\u001B[31mID inválido! Digite apenas números.\u001B[0m");
        }
    }

    private BigDecimal lerBigDecimalValido(LineReader reader, String mensagem) {
        while (true) {
            String valorStr = reader.readLine(mensagem).replace(",", ".").trim();
            try {
                BigDecimal valor = new BigDecimal(valorStr);
                if (valor.compareTo(BigDecimal.ZERO) > 0) return valor;
                System.out.println("\u001B[31mO valor deve ser maior que zero.\u001B[0m");
            } catch (Exception e) {
                System.out.println("\u001B[31mValor inválido! Ex: 150.00\u001B[0m");
            }
        }
    }

    private LocalDateTime lerDataHoraValida(LineReader reader, String mensagem) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        while (true) {
            try {
                String dataStr = reader.readLine(mensagem).trim();
                return LocalDateTime.parse(dataStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("\u001B[31mFormato inválido! Use: dd/MM/yyyy HH:mm (Ex: 15/04/2026 14:30)\u001B[0m");
            }
        }
    }

}
