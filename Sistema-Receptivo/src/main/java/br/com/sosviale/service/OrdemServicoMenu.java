package br.com.sosviale.service;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.model.Veiculo;
import br.com.sosviale.repository.MotoristaRepository;
import br.com.sosviale.repository.OrdemServicoRepository;
import br.com.sosviale.repository.TransferRepository;
import br.com.sosviale.repository.VeiculoRepository;
import org.jline.reader.LineReader;

import java.util.List;

public class OrdemServicoMenu {

    private final OrdemServicoRepository osRepo;
    private final MotoristaRepository motoristaRepo;
    private final VeiculoRepository veiculoRepo;
    private final TransferRepository transferRepo;

    // Construtor
    public OrdemServicoMenu(OrdemServicoRepository osRepo, MotoristaRepository motoristaRepo, VeiculoRepository veiculoRepo, TransferRepository transferRepo) {
        this.osRepo = osRepo;
        this.motoristaRepo = motoristaRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
    }

    public void menuOrdemServico(LineReader reader) {
        System.out.println("\n\u001B[35m--- GESTÃO DE ORDENS DE SERVIÇO (FROTA) --- \u001B[0m");
        System.out.println("\u001B[32m[1]\u001B[0m Abrir Nova OS (Definir Motorista e Veículo)");
        System.out.println("\u001B[32m[2]\u001B[0m Atribuir Transfers a uma OS (Montar a Rota)");
        System.out.println("\u001B[32m[3]\u001B[0m Voltar");

        try {
            String op = reader.readLine("Escolha: ").trim();
            switch (op) {
                case "1" -> abrirNovaOS(reader);
                case "2" -> selecionarOSParaMontagem(reader); // novo fluxo: lista OSes → pede ID → monta rota
                case "3" -> { return; }
                default  -> System.out.println("Opção inválida.");
            }
        } catch (Exception e) {
            // Captura erros inesperados de leitura do terminal ou acesso ao banco
            System.out.println("\u001B[31mErro no menu de OS: " + e.getMessage() + "\u001B[0m");
        }
    }


    private void selecionarOSParaMontagem(LineReader reader) throws Exception {
        System.out.println("\n\u001B[36m--- ORDENS DE SERVIÇO CADASTRADAS --- \u001B[0m");

        // Busca todas as OSes do banco para o usuário escolher
        List<OrdemServico> ordens;
        try {
            ordens = osRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] Não foi possível carregar as Ordens de Serviço: " + e.getMessage() + "\u001B[0m");
            return;
        }

        if (ordens.isEmpty()) {
            System.out.println("\u001B[33mNenhuma Ordem de Serviço encontrada. Abra uma nova OS primeiro (opção 1).\u001B[0m");
            return;
        }

        // Exibe a tabela de OSes para o usuário identificar qual quer montar
        System.out.println(String.format("%-5s | %-12s | %-25s | %-20s | %-8s",
                "ID", "DATA", "MOTORISTA", "VEÍCULO", "STATUS"));
        System.out.println("------------------------------------------------------------------------");
        for (OrdemServico os : ordens) {
            String nomeMotorista = (os.getMotorista() != null) ? os.getMotorista().getNome() : "Não atribuído";
            String nomeVeiculo   = (os.getVeiculo()   != null) ? os.getVeiculo().getLabel()  : "Não atribuído";
            System.out.println(String.format("%-5d | %-12s | %-25s | %-20s | %-8s",
                    os.getId(),
                    os.getDataServico(),
                    nomeMotorista,
                    nomeVeiculo,
                    os.getStatus()));
        }
        System.out.println("------------------------------------------------------------------------");

        // Solicita o ID da OS que o usuário deseja montar
        Integer idOs = lerIdValido(reader, "\nDigite o ID da OS que deseja montar: ").intValue();

        OrdemServico osSelecionada;
        try {
            osSelecionada = osRepo.buscarPorId(idOs);
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] Falha ao buscar a OS: " + e.getMessage() + "\u001B[0m");
            return;
        }

        if (osSelecionada == null) {
            System.out.println("\u001B[31mOS #" + idOs + " não encontrada. Verifique o ID e tente novamente.\u001B[0m");
            return;
        }

        // Com a OS válida em mãos, entra no loop de montagem de rota
        gerenciarTransfersDaOS(reader, osSelecionada);
    }

    private void abrirNovaOS(LineReader reader) throws Exception {
        System.out.println("\n\u001B[36m--- ABERTURA DE ORDEM DE SERVIÇO --- \u001B[0m");

        // Exibe motoristas disponíveis para o operador escolher
        listarMotoristas();
        Long idMotorista = lerIdValido(reader, "ID do Motorista para esta OS: ");
        Motorista motorista;
        try {
            motorista = motoristaRepo.buscarPorId(idMotorista);
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] Falha ao buscar motorista: " + e.getMessage() + "\u001B[0m");
            return;
        }

        // Exibe veículos disponíveis da frota para o operador escolher
        listarVeiculos();
        Long idVeiculo = lerIdValido(reader, "ID do Veículo: ");
        Veiculo veiculo;
        try {
            veiculo = veiculoRepo.buscarPorId(idVeiculo);
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] Falha ao buscar veículo: " + e.getMessage() + "\u001B[0m");
            return;
        }

        // Ambos precisam existir para criar a OS
        if (motorista == null || veiculo == null) {
            System.out.println("\u001B[31mMotorista ou Veículo não encontrado! Verifique os IDs.\u001B[0m");
            return;
        }

        // Cria a OS com a data de hoje; status padrão é "ABERTA"
        OrdemServico os = new OrdemServico();
        os.setDataServico(java.time.LocalDate.now());
        os.setMotorista(motorista);
        os.setVeiculo(veiculo);

        try {
            osRepo.salvar(os);
            System.out.println("\u001B[32m✔ OS #" + os.getId() + " aberta com sucesso para o motorista " + motorista.getNome() + "!\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] Não foi possível salvar a OS: " + e.getMessage() + "\u001B[0m");
        }
    }


    private void gerenciarTransfersDaOS(LineReader reader, OrdemServico os) throws Exception {
        System.out.println("\n\u001B[33mMontando OS #" + os.getId() + " | Motorista: " + os.getMotorista().getNome()
                + " | Veículo: " + os.getVeiculo().getLabel()
                + " | Capacidade: " + os.getVeiculo().getCapacidade() + " pax\u001B[0m");

        boolean adicionando = true;
        while (adicionando) {

            // Busca todos os transfers e filtra os que ainda não têm OS e estão AGENDADOS
            List<Transfer> todosTransfers;
            try {
                todosTransfers = transferRepo.listarTodos();
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO DB] Falha ao carregar transfers: " + e.getMessage() + "\u001B[0m");
                break;
            }

            System.out.println("\nTransfers Agendados e Disponíveis (sem OS atribuída):");
            boolean temDisponivel = false;
            for (Transfer t : todosTransfers) {
                if (t.getOrdemServico() == null && t.getStatus() == StatusTransfer.AGENDADO) {
                    System.out.println(String.format("  ID: %-4d | %s | De: %-15s Para: %-15s | Pax: %d",
                            t.getId(), t.getDataHora(), t.getOrigem(), t.getDestino(), t.getPassageiros().size()));
                    temDisponivel = true;
                }
            }

            // Se não há mais transfers disponíveis, encerra o loop automaticamente
            if (!temDisponivel) {
                System.out.println("\u001B[33mNenhum transfer disponível para adicionar.\u001B[0m");
                break;
            }

            String idAdd = reader.readLine("\nDigite o ID do Transfer para adicionar à OS (ou 'fim'): ").trim();
            if (idAdd.equalsIgnoreCase("fim")) break;

            // Valida que o input é numérico antes de ir ao banco
            if (!isApenasNumeros(idAdd)) {
                System.out.println("\u001B[31mID inválido! Digite apenas números ou 'fim'.\u001B[0m");
                continue;
            }

            Transfer transferEscolhido;
            try {
                transferEscolhido = transferRepo.buscarPorId(Long.parseLong(idAdd));
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO DB] Falha ao buscar o transfer: " + e.getMessage() + "\u001B[0m");
                continue;
            }

            // Verifica se o transfer existe e ainda não está vinculado a nenhuma OS
            if (transferEscolhido == null || transferEscolhido.getOrdemServico() != null) {
                System.out.println("\u001B[31mTransfer inválido ou já atribuído a outra OS.\u001B[0m");
                continue;
            }

            // REGRA DE NEGÓCIO 1: Capacidade
            // Calcula o total de passageiros já alocados nesta OS somando os de cada transfer.
            // O novo transfer só pode ser adicionado se a soma total não exceder a capacidade do veículo.
            int totalPassageirosNaOs = os.getTransfers().stream()
                    .mapToInt(t -> t.getPassageiros().size())
                    .sum();
            int totalComNovoTransfer = totalPassageirosNaOs + transferEscolhido.getPassageiros().size();
            if (totalComNovoTransfer > os.getVeiculo().getCapacidade()) {
                System.out.println("\u001B[31m[BLOQUEADO] Capacidade excedida! Veículo suporta "
                        + os.getVeiculo().getCapacidade() + " pax. Já há " + totalPassageirosNaOs
                        + " na OS, e este transfer tem " + transferEscolhido.getPassageiros().size()
                        + " (total seria " + totalComNovoTransfer + ").\u001B[0m");
                continue;
            }

            // REGRA DE NEGÓCIO 2: Cronologia
            // O motorista não pode ter dois transfers ao mesmo tempo ou em ordem invertida.
            // O novo transfer deve ter dataHora estritamente POSTERIOR ao último já adicionado.
            if (!os.getTransfers().isEmpty()) {
                Transfer ultimoTransfer = os.getTransfers().get(os.getTransfers().size() - 1);
                if (!transferEscolhido.getDataHora().isAfter(ultimoTransfer.getDataHora())) {
                    System.out.println("\u001B[31m[BLOQUEADO] Conflito de horário! O último transfer está marcado para "
                            + ultimoTransfer.getDataHora() + ". Este novo começa às "
                            + transferEscolhido.getDataHora() + " (deve ser posterior).\u001B[0m");
                    continue;
                }
            }

            // Passou nas duas validações: vincula este transfer à OS e persiste no banco
            try {
                transferEscolhido.setOrdemServico(os);
                transferRepo.atualizar(transferEscolhido);

                // Atualiza a lista em memória para que as próximas validações do loop
                // já considerem este transfer como parte da OS
                os.getTransfers().add(transferEscolhido);

                System.out.println("\u001B[32m✔ Transfer #" + transferEscolhido.getId() + " adicionado à rota da OS!\u001B[0m");
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO DB] Não foi possível salvar o vínculo: " + e.getMessage() + "\u001B[0m");
            }
        }

        System.out.println("\u001B[32m\nRota da OS #" + os.getId() + " finalizada com "
                + os.getTransfers().size() + " transfer(s).\u001B[0m");
    }

    // --- MÉTODOS AUXILIARES QUE FALTAVAM ---

    private void listarMotoristas() {
        System.out.println("\n\u001B[36m--- MOTORISTAS CADASTRADOS --- \u001B[0m");
        List<Motorista> lista = motoristaRepo.listarTodos();
        System.out.println(String.format("%-5s | %-25s | %-15s", "ID", "NOME", "CNH"));
        for (Motorista m : lista) {
            System.out.println(String.format("%-5d | %-25s | %-15s", m.getId(), m.getNome(), m.getCnh()));
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

    private Long lerIdValido(LineReader reader, String mensagem) {
        while (true) {
            String idStr = reader.readLine(mensagem).trim();
            if (isApenasNumeros(idStr)) return Long.parseLong(idStr);
            System.out.println("\u001B[31mID inválido! Digite apenas números.\u001B[0m");
        }
    }

    private boolean isApenasNumeros(String dados) {
        return dados != null && dados.matches("\\d+");
    }
}