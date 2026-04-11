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

    // construtor com injeção de dependências
    public OrdemServicoMenu(OrdemServicoRepository osRepo, MotoristaRepository motoristaRepo,
                            VeiculoRepository veiculoRepo, TransferRepository transferRepo) {
        this.osRepo = osRepo;
        this.motoristaRepo = motoristaRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
    }

    /**
     * ponto de entrada do menu de ordens de serviço.
     * [1] abre uma nova OS definindo motorista e veículo
     * [2] lista OSes e permite montar a rota de uma delas
     * [3] gera o PDF de uma OS existente
     * [4] volta ao menu principal
     */
    public void menuOrdemServico(LineReader reader) {
        boolean noMenuOS = true;
        while (noMenuOS) {
            System.out.println("\n\u001B[35m--- GESTÃO DE ORDENS DE SERVIÇO (FROTA) --- \u001B[0m");
            System.out.println("\u001B[32m[1]\u001B[0m Abrir Nova OS (Definir Motorista e Veículo)");
            System.out.println("\u001B[32m[2]\u001B[0m Atribuir Transfers a uma OS (Montar a Rota)");
            System.out.println("\u001B[32m[3]\u001B[0m Gerar PDF da Ordem de Serviço");
            System.out.println("\u001B[32m[4]\u001B[0m Voltar");

            try {
                String op = reader.readLine("Escolha: ").trim();
                switch (op) {
                    case "1" -> abrirNovaOS(reader);
                    case "2" -> selecionarOSParaMontagem(reader);
                    case "3" -> menuGerarPdf(reader);
                    case "4" -> noMenuOS = false;
                    default  -> System.out.println("\u001B[31mOpção inválida.\u001B[0m");
                }
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO INESPERADO]: " + e.getMessage() + "\u001B[0m");
            }
        }
    }

    /**
     * fluxo da opção [2]:
     * exibe todas as OSes, solicita o ID desejado e encaminha para
     * gerenciarTransfersDaOS com a OS já carregada do banco.
     */
    private void selecionarOSParaMontagem(LineReader reader) throws Exception {
        System.out.println("\n\u001B[36m--- ORDENS DE SERVIÇO CADASTRADAS --- \u001B[0m");

        List<OrdemServico> ordens;
        try {
            ordens = osRepo.listarTodos();
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] não foi possível carregar as ordens de serviço: " + e.getMessage() + "\u001B[0m");
            return;
        }

        if (ordens.isEmpty()) {
            System.out.println("\u001B[33mNenhuma Ordem de Serviço encontrada. Abra uma nova OS primeiro (opção 1).\u001B[0m");
            return;
        }

        System.out.println(String.format("%-5s | %-12s | %-25s | %-20s | %-8s",
                "ID", "DATA", "MOTORISTA", "VEÍCULO", "STATUS"));
        System.out.println("------------------------------------------------------------------------");
        for (OrdemServico os : ordens) {
            String nomeMotorista = (os.getMotorista() != null) ? os.getMotorista().getNome() : "Não atribuído";
            String nomeVeiculo   = (os.getVeiculo()   != null) ? os.getVeiculo().getLabel()  : "Não atribuído";
            System.out.println(String.format("%-5d | %-12s | %-25s | %-20s | %-8s",
                    os.getId(), os.getDataServico(), nomeMotorista, nomeVeiculo, os.getStatus()));
        }
        System.out.println("------------------------------------------------------------------------");

        Integer idOs = lerIdValido(reader, "\nDigite o ID da OS que deseja montar: ").intValue();

        OrdemServico osSelecionada;
        try {
            osSelecionada = osRepo.buscarPorId(idOs);
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] falha ao buscar a OS: " + e.getMessage() + "\u001B[0m");
            return;
        }

        if (osSelecionada == null) {
            System.out.println("\u001B[31mOS #" + idOs + " não encontrada. Verifique o ID e tente novamente.\u001B[0m");
            return;
        }

        gerenciarTransfersDaOS(reader, osSelecionada);
    }

    /**
     * abre uma nova OS definindo motorista, veículo e data (hoje).
     * valida que ambos existem antes de persistir.
     */
    private void abrirNovaOS(LineReader reader) throws Exception {
        System.out.println("\n\u001B[36m--- ABERTURA DE ORDEM DE SERVIÇO --- \u001B[0m");

        listarMotoristas();
        Long idMotorista = lerIdValido(reader, "ID do Motorista para esta OS: ");
        Motorista motorista;
        try {
            motorista = motoristaRepo.buscarPorId(idMotorista);
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] falha ao buscar motorista: " + e.getMessage() + "\u001B[0m");
            return;
        }

        if (motorista == null) {
            System.out.println("\u001B[31mMotorista não encontrado! Verifique o ID.\u001B[0m");
            return;
        }

        listarVeiculos();
        Long idVeiculo = lerIdValido(reader, "ID do Veículo: ");
        Veiculo veiculo;
        try {
            veiculo = veiculoRepo.buscarPorId(idVeiculo);
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] falha ao buscar veículo: " + e.getMessage() + "\u001B[0m");
            return;
        }

        if (veiculo == null) {
            System.out.println("\u001B[31mVeículo não encontrado! Verifique o ID.\u001B[0m");
            return;
        }

        // cria a OS com data de hoje; status padrão "ABERTA" definido na entidade
        OrdemServico os = new OrdemServico();
        os.setDataServico(java.time.LocalDate.now());
        os.setMotorista(motorista);
        os.setVeiculo(veiculo);

        try {
            osRepo.salvar(os);
            System.out.println("\u001B[32m✔ OS #" + os.getId() + " aberta com sucesso para o motorista " + motorista.getNome() + "!\u001B[0m");
        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO DB] não foi possível salvar a OS: " + e.getMessage() + "\u001B[0m");
        }
    }

    /**
     * loop de montagem de rota para a OS selecionada.
     * regra 1 (lotação): soma de passageiros não pode exceder a capacidade do veículo.
     * regra 2 (cronologia): cada transfer adicionado deve ter horário posterior ao anterior,
     *                       evitando conflitos de agenda do motorista.
     */
    private void gerenciarTransfersDaOS(LineReader reader, OrdemServico os) throws Exception {
        System.out.println("\n\u001B[33mMontando OS #" + os.getId()
                + " | Motorista: " + os.getMotorista().getNome()
                + " | Veículo: " + os.getVeiculo().getLabel()
                + " | Capacidade: " + os.getVeiculo().getCapacidade() + " pax\u001B[0m");

        boolean adicionando = true;
        while (adicionando) {

            List<Transfer> todosTransfers;
            try {
                todosTransfers = transferRepo.listarTodos();
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO DB] falha ao carregar transfers: " + e.getMessage() + "\u001B[0m");
                break;
            }

            System.out.println("\nTransfers agendados e disponíveis (sem OS atribuída):");
            boolean temDisponivel = false;
            for (Transfer t : todosTransfers) {
                if (t.getOrdemServico() == null && t.getStatus() == StatusTransfer.AGENDADO) {
                    System.out.println(String.format("  ID: %-4d | %s | De: %-15s Para: %-15s | Pax: %d",
                            t.getId(), t.getDataHora(), t.getOrigem(), t.getDestino(), t.getPassageiros().size()));
                    temDisponivel = true;
                }
            }

            if (!temDisponivel) {
                System.out.println("\u001B[33mNenhum transfer disponível para adicionar.\u001B[0m");
                break;
            }

            String idAdd = reader.readLine("\nDigite o ID do Transfer para adicionar à OS (ou 'fim'): ").trim();
            if (idAdd.equalsIgnoreCase("fim")) break;

            if (!isApenasNumeros(idAdd)) {
                System.out.println("\u001B[31mID inválido! Digite apenas números ou 'fim'.\u001B[0m");
                continue;
            }

            Transfer transferEscolhido;
            try {
                transferEscolhido = transferRepo.buscarPorId(Long.parseLong(idAdd));
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO DB] falha ao buscar o transfer: " + e.getMessage() + "\u001B[0m");
                continue;
            }

            if (transferEscolhido == null || transferEscolhido.getOrdemServico() != null) {
                System.out.println("\u001B[31mTransfer inválido ou já atribuído a outra OS.\u001B[0m");
                continue;
            }

            // regra 1 — verifica se a adição deste transfer não estoura a capacidade do veículo
            int totalPassageirosNaOs = os.getTransfers().stream()
                    .mapToInt(t -> t.getPassageiros().size())
                    .sum();
            int totalComNovoTransfer = totalPassageirosNaOs + transferEscolhido.getPassageiros().size();
            if (totalComNovoTransfer > os.getVeiculo().getCapacidade()) {
                System.out.println("\u001B[31m[BLOQUEADO] capacidade excedida! Veículo suporta "
                        + os.getVeiculo().getCapacidade() + " pax. Já há " + totalPassageirosNaOs
                        + " na OS, e este transfer tem " + transferEscolhido.getPassageiros().size()
                        + " (total seria " + totalComNovoTransfer + ").\u001B[0m");
                continue;
            }

            // regra 2 — o novo transfer deve ter horário estritamente posterior ao último adicionado
            if (!os.getTransfers().isEmpty()) {
                Transfer ultimoTransfer = os.getTransfers().get(os.getTransfers().size() - 1);
                if (!transferEscolhido.getDataHora().isAfter(ultimoTransfer.getDataHora())) {
                    System.out.println("\u001B[31m[BLOQUEADO] conflito de horário! O último transfer está marcado para "
                            + ultimoTransfer.getDataHora() + ". Este novo começa às "
                            + transferEscolhido.getDataHora() + " (deve ser posterior).\u001B[0m");
                    continue;
                }
            }

            // passou nas duas validações: vincula e persiste
            try {
                transferEscolhido.setOrdemServico(os);
                transferRepo.atualizar(transferEscolhido);
                // atualiza a lista em memória para que as próximas iterações do loop já incluam este transfer
                os.getTransfers().add(transferEscolhido);
                System.out.println("\u001B[32m✔ Transfer #" + transferEscolhido.getId() + " adicionado à rota da OS!\u001B[0m");
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO DB] não foi possível salvar o vínculo: " + e.getMessage() + "\u001B[0m");
            }
        }

        System.out.println("\u001B[32m\nRota da OS #" + os.getId() + " finalizada com "
                + os.getTransfers().size() + " transfer(s).\u001B[0m");
    }

    /**
     * solicita o ID de uma OS e gera o PDF correspondente via PdfItext.
     */
    private void menuGerarPdf(LineReader reader) {
        System.out.println("\n\u001B[36m--- EXPORTAR OS PARA PDF --- \u001B[0m");
        try {
            Long idOs = lerIdValido(reader, "Digite o ID da Ordem de Serviço: ");
            OrdemServico os = osRepo.buscarPorId(idOs.intValue());

            if (os == null) {
                System.out.println("\u001B[31mOrdem de Serviço não encontrada!\u001B[0m");
                return;
            }

            System.out.println("\u001B[33mGerando documento...\u001B[0m");
            br.com.sosviale.util.PdfItext.gerarPdfOs(os);
            System.out.println("\u001B[32m✔ PDF da OS #" + os.getId() + " gerado com sucesso na pasta do projeto!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO GERAR PDF]: " + e.getMessage() + "\u001B[0m");
            e.printStackTrace();
        }
    }

    // --- métodos auxiliares ---

    private void listarMotoristas() {
        System.out.println("\n\u001B[36m--- MOTORISTAS CADASTRADOS --- \u001B[0m");
        List<Motorista> lista = motoristaRepo.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum motorista cadastrado.");
            return;
        }
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

    // lê um ID numérico positivo; repete o prompt até receber entrada válida
    private Long lerIdValido(LineReader reader, String mensagem) {
        while (true) {
            String idStr = reader.readLine(mensagem).trim();
            if (isApenasNumeros(idStr) && Long.parseLong(idStr) > 0) return Long.parseLong(idStr);
            System.out.println("\u001B[31mID inválido! Digite apenas números positivos.\u001B[0m");
        }
    }

    // verifica se a string contém apenas dígitos numéricos
    private boolean isApenasNumeros(String dados) {
        return dados != null && !dados.isEmpty() && dados.matches("\\d+");
    }
}
