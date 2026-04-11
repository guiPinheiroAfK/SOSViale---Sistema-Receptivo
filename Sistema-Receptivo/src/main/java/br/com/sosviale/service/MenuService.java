package br.com.sosviale.service;

import br.com.sosviale.repository.*;
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

    // repositórios injetados pelo construtor
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private OrdemServicoRepository osRepo;

    // construtor que recebe os repositórios instanciados pela Main
    public MenuService(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo,
                       TransferRepository transferRepo, MotoristaRepository motoristaRepo,
                       PontoColetaRepository pontoColetaRepo, OrdemServicoRepository osRepo) {
        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
        this.motoristaRepo = motoristaRepo;
        this.pontoColetaRepo = pontoColetaRepo;
        this.osRepo = osRepo;
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
            System.err.println("erro na interface do terminal: " + e.getMessage());
        }
    }

    // ----- TRANSFER -----

    private void agendarTransfer(LineReader reader) {
        System.out.println("\n\u001B[36m--- NOVO AGENDAMENTO DE TRANSFER --- \u001B[0m");
        try {
            String origem = lerStringObrigatoria(reader, "Origem (Ex: Aeroporto IGU): ");
            String destino = lerStringObrigatoria(reader, "Destino (Ex: Hotel Cataratas): ");

            // destino não pode ser igual à origem
            if (origem.equalsIgnoreCase(destino)) {
                System.out.println("\u001B[31m[ERRO]: origem e destino não podem ser iguais.\u001B[0m");
                return;
            }

            BigDecimal valor = lerBigDecimalValido(reader, "Valor Total do Transfer (R$): ");
            LocalDateTime dataHora = lerDataHoraValida(reader, "Data e Hora (dd/MM/yyyy HH:mm): ");

            // o transfer nasce sem OS; motorista e veículo são definidos depois
            Transfer novoTransfer = new Transfer(dataHora, origem, destino, valor);
            novoTransfer.setStatus(StatusTransfer.AGENDADO);

            // exibe passageiros existentes se solicitado
            String ver2 = reader.readLine("Deseja listar passageiros cadastrados? (s/n): ");
            if (ver2.equalsIgnoreCase("s")) {
                listarPassageiros();
            }

            System.out.println("\n\u001B[33m--- ADICIONANDO PASSAGEIROS --- \u001B[0m");
            System.out.println("A capacidade será validada ao atribuir o veículo na OS.");

            boolean adicionando = true;
            while (adicionando) {
                String pIdStr = reader.readLine("ID do Passageiro (ou 'fim'): ").trim();
                if (pIdStr.equalsIgnoreCase("fim")) {
                    if (novoTransfer.getPassageiros().isEmpty()) {
                        System.out.println("\u001B[31mErro: informe pelo menos 1 passageiro!\u001B[0m");
                        continue;
                    }
                    adicionando = false;
                } else {
                    if (!isApenasNumeros(pIdStr)) {
                        System.out.println("\u001B[31mID inválido! Digite apenas números.\u001B[0m");
                        continue;
                    }
                    Passageiro p = passageiroRepo.buscarPorId(Long.parseLong(pIdStr));
                    if (p == null) {
                        System.out.println("\u001B[31mPassageiro não encontrado.\u001B[0m");
                    } else if (novoTransfer.getPassageiros().contains(p)) {
                        System.out.println("\u001B[31mPassageiro já adicionado!\u001B[0m");
                    } else {
                        novoTransfer.getPassageiros().add(p);
                        System.out.println("\u001B[32m✔ " + p.getNome() + " adicionado.\u001B[0m");
                    }
                }
            }

            transferRepo.salvar(novoTransfer);
            System.out.println("\n\u001B[32m✔ Transfer [" + novoTransfer.getStatus() + "] salvo e aguardando Ordem de Serviço!\u001B[0m");

            // cadastro opcional de pontos de coleta intermediários
            System.out.println("\n--- PONTOS DE COLETA (opcional) ---");
            System.out.println("Digite 'fim' para encerrar.");
            int ordem = 1;
            while (true) {
                String local = reader.readLine("Parada " + ordem + " - Local (ou 'fim'): ").trim();
                if (local.equalsIgnoreCase("fim")) break;
                if (local.isEmpty()) {
                    System.out.println("\u001B[31mNome do local não pode ser vazio.\u001B[0m");
                    continue;
                }
                String horario = reader.readLine("Horário previsto (HH:mm, ou Enter para pular): ").trim();

                PontoColeta ponto = new PontoColeta();
                ponto.setTransfer(novoTransfer);
                ponto.setLocalColeta(local);
                ponto.setOrdemParada(ordem++);
                if (!horario.isBlank()) {
                    try {
                        ponto.setHorarioPrevisto(LocalTime.parse(horario, DateTimeFormatter.ofPattern("HH:mm")));
                    } catch (DateTimeParseException e) {
                        System.out.println("\u001B[33m[AVISO]: horário inválido, ponto salvo sem horário previsto.\u001B[0m");
                    }
                }
                // coordenadas zeradas até integração com geocodificação
                ponto.setLatitude(0.0);
                ponto.setLongitude(0.0);
                pontoColetaRepo.salvar(ponto);
                System.out.println("\u001B[32m✔ Ponto de coleta salvo.\u001B[0m");
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

    private void editarTransfer(LineReader reader) {
        try {
            Long id = lerIdValido(reader, "ID do Transfer para editar: ");
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

    private void excluirTransfer(LineReader reader) {
        try {
            Long id = lerIdValido(reader, "ID do Transfer para cancelar: ");
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
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }

    // ----- PASSAGEIRO -----

    private void cadastrarPassageiro(LineReader reader) {
        String opcao;
        do {
            System.out.println("\n\u001B[36m--- CADASTRO DE PASSAGEIRO --- \u001B[0m");
            try {
                String nome = lerNomeValido(reader, "Nome Completo: ");
                String documento = reader.readLine("Documento (RG/Passaporte): ").trim();
                String nacionalidade = lerStringObrigatoria(reader, "Nacionalidade: ");

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

    private void listarPassageiros() {
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

    private void excluirPassageiro(LineReader reader) {
        System.out.println("\n\u001B[31m--- EXCLUIR PASSAGEIRO --- \u001B[0m");
        try {
            Long id = lerIdValido(reader, "ID do passageiro para excluir: ");
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

    private void editarPassageiro(LineReader reader) {
        System.out.println("\n\u001B[34m--- EDITAR PASSAGEIRO --- \u001B[0m");
        try {
            Long id = lerIdValido(reader, "ID do passageiro: ");
            Passageiro p = passageiroRepo.buscarPorId(id);
            if (p == null) {
                System.out.println("Passageiro não encontrado.");
                return;
            }

            String novoNome = reader.readLine("Novo Nome [" + p.getNome() + "]: ").trim();
            if (!novoNome.isEmpty()) {
                if (!isNomeValido(novoNome)) {
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
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }

    // ----- MOTORISTA -----

    private void cadastrarMotorista(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE MOTORISTA --- \u001B[0m");
        try {
            String nome = lerNomeValido(reader, "Nome Completo: ").toUpperCase().trim();

            // CNH deve ter exatamente 11 dígitos numéricos
            String cnh = "";
            while (true) {
                cnh = reader.readLine("Número da CNH (11 dígitos): ").trim();
                if (cnh.length() == 11 && isApenasNumeros(cnh)) break;
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

    private void listarMotoristas() {
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

    private void editarMotorista(LineReader reader) {
        System.out.println("\n\u001B[34m--- EDITAR MOTORISTA --- \u001B[0m");
        try {
            Long id = lerIdValido(reader, "ID do motorista: ");
            Motorista m = motoristaRepo.buscarPorId(id);
            if (m == null) {
                System.out.println("\u001B[31m[ERRO]: motorista não encontrado.\u001B[0m");
                return;
            }

            String novoNome = reader.readLine("Novo Nome [" + m.getNome() + "]: ").trim();
            if (!novoNome.isEmpty()) {
                if (!isNomeValido(novoNome)) {
                    System.out.println("\u001B[31mNome inválido! Mantendo o anterior.\u001B[0m");
                } else {
                    m.setNome(novoNome.toUpperCase());
                }
            }

            String novaCnh = reader.readLine("Nova CNH [" + m.getCnh() + "]: ").trim();
            if (!novaCnh.isEmpty()) {
                if (novaCnh.length() != 11 || !isApenasNumeros(novaCnh)) {
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

    private void excluirMotorista(LineReader reader) {
        System.out.println("\n\u001B[31m--- EXCLUIR MOTORISTA --- \u001B[0m");
        try {
            Long id = lerIdValido(reader, "ID para excluir: ");
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
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }

    // ----- VEÍCULO -----

    private void cadastrarVeiculo(LineReader reader) {
        System.out.println("\n\u001B[36m--- CADASTRO DE VEÍCULO --- \u001B[0m");
        try {
            String label = lerStringObrigatoria(reader, "Modelo (Ex: Mercedes Sprinter): ").toUpperCase().trim();

            // placa no padrão Mercosul (ABC1D23)
            String placa = "";
            while (true) {
                placa = reader.readLine("Placa (padrão Mercosul, ex: ABC1D23): ").toUpperCase().trim();
                if (isPlacaValida(placa)) break;
                System.out.println("\u001B[31mPlaca inválida! Use o padrão Mercosul (ABC1D23).\u001B[0m");
            }

            int capacidade = 0;
            while (true) {
                String capStr = reader.readLine("Capacidade de passageiros: ").trim();
                if (isApenasNumeros(capStr) && Integer.parseInt(capStr) > 0) {
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

    private void listarVeiculos() {
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

    private void editarVeiculo(LineReader reader) {
        try {
            Long id = lerIdValido(reader, "ID do veículo: ");
            Veiculo v = veiculoRepo.buscarPorId(id);
            if (v == null) {
                System.out.println("\u001B[31mVeículo não encontrado.\u001B[0m");
                return;
            }

            String novoModelo = reader.readLine("Novo Modelo [" + v.getLabel() + "]: ").trim();
            if (!novoModelo.isEmpty()) v.setLabel(novoModelo.toUpperCase());

            String novaPlaca = reader.readLine("Nova Placa [" + v.getPlaca() + "]: ").trim().toUpperCase();
            if (!novaPlaca.isEmpty()) {
                if (!isPlacaValida(novaPlaca)) {
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

    private void excluirVeiculo(LineReader reader) {
        try {
            Long id = lerIdValido(reader, "ID para excluir: ");
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
            case "5": return;
            default: System.out.println("Opção inválida.");
        }
    }

    // ----- MÉTODOS DE VALIDAÇÃO -----

    // retorna true se o nome contém apenas letras e espaços com no mínimo 3 caracteres
    public static boolean isNomeValido(String nome) {
        return nome != null && nome.trim().matches("^[a-zA-ZÀ-ÿ\\s]{3,}$");
    }

    // retorna true se a string contém apenas dígitos numéricos
    public static boolean isApenasNumeros(String dados) {
        return dados != null && !dados.isEmpty() && dados.matches("\\d+");
    }

    // valida placa no padrão Mercosul (ex: ABC1D23)
    public static boolean isPlacaValida(String placa) {
        return placa != null && placa.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}");
    }

    // ----- MÉTODOS AUXILIARES DE LEITURA -----

    // lê uma string não vazia; repete o prompt até receber entrada válida
    private String lerStringObrigatoria(LineReader reader, String mensagem) {
        while (true) {
            String valor = reader.readLine(mensagem).trim();
            if (!valor.isEmpty()) return valor;
            System.out.println("\u001B[31mEste campo é obrigatório!\u001B[0m");
        }
    }

    // lê um nome válido (apenas letras, mín. 3 chars); repete até receber entrada válida
    private String lerNomeValido(LineReader reader, String mensagem) {
        while (true) {
            String nome = reader.readLine(mensagem).trim();
            if (isNomeValido(nome)) return nome;
            System.out.println("\u001B[31mNome inválido! Use apenas letras (mín. 3).\u001B[0m");
        }
    }

    // lê um ID numérico positivo; repete até receber entrada válida
    private Long lerIdValido(LineReader reader, String mensagem) {
        while (true) {
            String idStr = reader.readLine(mensagem).trim();
            if (isApenasNumeros(idStr) && Long.parseLong(idStr) > 0) return Long.parseLong(idStr);
            System.out.println("\u001B[31mID inválido! Digite apenas números positivos.\u001B[0m");
        }
    }

    // lê um BigDecimal maior que zero; repete até receber entrada válida
    private BigDecimal lerBigDecimalValido(LineReader reader, String mensagem) {
        while (true) {
            String valorStr = reader.readLine(mensagem).replace(",", ".").trim();
            try {
                BigDecimal valor = new BigDecimal(valorStr);
                if (valor.compareTo(BigDecimal.ZERO) > 0) return valor;
                System.out.println("\u001B[31mO valor deve ser maior que zero.\u001B[0m");
            } catch (NumberFormatException e) {
                System.out.println("\u001B[31mValor inválido! Ex: 150.00\u001B[0m");
            }
        }
    }

    // lê uma data/hora no formato dd/MM/yyyy HH:mm; repete até receber entrada válida
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