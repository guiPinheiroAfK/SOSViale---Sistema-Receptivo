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
    // todo fala time, aqui ó, precisamos de validação em tudo desse arquivo ok
    // Adicione a opção no menu principal e chame este metodo:
    public void menuOrdemServico(LineReader reader) {
        boolean noMenuOS = true;
        while (noMenuOS) {
            System.out.println("\n\u001B[35m--- GESTÃO DE ORDENS DE SERVIÇO (FROTA) --- \u001B[0m");
            System.out.println("\u001B[32m[1]\u001B[0m Abrir Nova OS (Definir Motorista e Veículo)");
            System.out.println("\u001B[32m[2]\u001B[0m Atribuir Transfers a uma OS (Montar a Rota)");
            System.out.println("\u001B[32m[3]\u001B[0m Gerar PDF da Ordem de Serviço"); // <-- NOVA OPÇÃO!
            System.out.println("\u001B[32m[4]\u001B[0m Voltar");

            try {
                String op = reader.readLine("Escolha: ").trim();
                switch (op) {
                    case "1" -> abrirNovaOS(reader);
                    case "2" -> gerenciarTransfersDaOS(reader);
                    case "3" -> menuGerarPdf(reader);// <-- chama o pdf
                    case "4" -> { noMenuOS = false; } // Sai do loop e volta pro menu principal
                    default -> System.out.println("\u001B[31mOpção inválida.\u001B[0m");
                }
            } catch (Exception e) {
                System.out.println("\u001B[31m[ERRO INESPERADO]: " + e.getMessage() + "\u001B[0m");
            }
        }
    }

    private void abrirNovaOS(LineReader reader) throws Exception {
        System.out.println("\n\u001B[36m--- ABERTURA DE ORDEM DE SERVIÇO --- \u001B[0m");

        listarMotoristas();
        Long idMotorista = lerIdValido(reader, "ID do Motorista para esta OS: ");
        Motorista motorista = motoristaRepo.buscarPorId(idMotorista);

        listarVeiculos();
        Long idVeiculo = lerIdValido(reader, "ID do Veículo: ");
        Veiculo veiculo = veiculoRepo.buscarPorId(idVeiculo);

        if (motorista == null || veiculo == null) {
            System.out.println("\u001B[31mMotorista ou Veículo não encontrado!\u001B[0m");
            return;
        }

        // Para simplificar, a data da OS será a data atual do sistema (ou você pode pedir pro usuário digitar)
        OrdemServico os = new OrdemServico();
        os.setDataServico(java.time.LocalDate.now());
        os.setMotorista(motorista);
        os.setVeiculo(veiculo);

        osRepo.salvar(os);
        System.out.println("\u001B[32m✔ OS #" + os.getId() + " aberta com sucesso para o motorista " + motorista.getNome() + "!\u001B[0m");
    }

    private void gerenciarTransfersDaOS(LineReader reader) throws Exception {
        // 1. Pede a OS
        Integer idOs = lerIdValido(reader, "Digite o ID da OS que deseja montar: ").intValue();
        OrdemServico os = osRepo.buscarPorId(idOs);

        if (os == null) {
            System.out.println("\u001B[31mOS não encontrada.\u001B[0m");
            return;
        }

        System.out.println("\n\u001B[33mMontando OS #" + os.getId() + " | Veículo Capacidade: " + os.getVeiculo().getCapacidade() + "\u001B[0m");

        boolean adicionando = true;
        while(adicionando) {
            // Mostrar transfers "soltos" (sem OS)
            System.out.println("\nTransfers Agendados e Disponíveis:");
            List<Transfer> todosTransfers = transferRepo.listarTodos();
            boolean temDisponivel = false;

            for (Transfer t : todosTransfers) {
                if (t.getOrdemServico() == null && t.getStatus() == StatusTransfer.AGENDADO) {
                    System.out.println("ID: " + t.getId() + " | " + t.getDataHora() + " | De: " + t.getOrigem() + " Para: " + t.getDestino() + " | Pax: " + t.getPassageiros().size());
                    temDisponivel = true;
                }
            }

            if (!temDisponivel) {
                System.out.println("Nenhum transfer solto disponível.");
                break;
            }

            String idAdd = reader.readLine("\nDigite o ID do Transfer para adicionar à OS (ou 'fim'): ");
            if (idAdd.equalsIgnoreCase("fim")) break;

            Transfer transferEscolhido = transferRepo.buscarPorId(Long.parseLong(idAdd));

            if (transferEscolhido != null && transferEscolhido.getOrdemServico() == null) {

                // REGRAS DE NEGÓCIO AQUI!

                // Regra 1: Lotação
                if (transferEscolhido.getPassageiros().size() > os.getVeiculo().getCapacidade()) {
                    System.out.println("\u001B[31m[BLOQUEADO] O veículo suporta " + os.getVeiculo().getCapacidade() + ", mas o transfer tem " + transferEscolhido.getPassageiros().size() + " passageiros.\u001B[0m");
                    continue;
                }

                // Regra 2: Viagem no Tempo (Pega o último transfer adicionado e compara o horário)
                if (!os.getTransfers().isEmpty()) {
                    Transfer ultimoTransfer = os.getTransfers().get(os.getTransfers().size() - 1);
                    if (transferEscolhido.getDataHora().isBefore(ultimoTransfer.getDataHora())) {
                        System.out.println("\u001B[31m[BLOQUEADO] Você não pode voltar no tempo! O último transfer acaba às " + ultimoTransfer.getDataHora() + ". Este novo começa às " + transferEscolhido.getDataHora() + ".\u001B[0m");
                        continue;
                    }
                }

                // Se passou nas validações, atrela a OS ao Transfer
                transferEscolhido.setOrdemServico(os);
                transferRepo.atualizar(transferEscolhido); // Salva a mudança no banco

                // Atualiza a lista em memória para as próximas validações do loop
                os.getTransfers().add(transferEscolhido);

                System.out.println("\u001B[32m✔ Transfer #" + transferEscolhido.getId() + " adicionado à rota da OS!\u001B[0m");

            } else {
                System.out.println("\u001B[31mTransfer inválido ou já atribuído.\u001B[0m");
            }
        }
    }

    // --- MÉTODOS AUXILIARES QUE FALTAVAM ---

    private void menuGerarPdf(LineReader reader) {
        System.out.println("\n\u001B[36m--- EXPORTAR OS PARA PDF --- \u001B[0m");
        try {
            // 1. Pergunta qual OS o usuário quer imprimir usando o reader
            Long idOsStr = lerIdValido(reader, "Digite o ID da Ordem de Serviço: ");

            // 2. Busca a OS de verdade no banco de dados
            OrdemServico os = osRepo.buscarPorId(idOsStr.intValue());

            if (os == null) {
                System.out.println("\u001B[31mOrdem de Serviço não encontrada!\u001B[0m");
                return;
            }

            System.out.println("\u001B[33mGerando documento...\u001B[0m");

            // 3. AGORA SIM! Passamos a 'os' de verdade para a classe PdfItext
            br.com.sosviale.util.PdfItext.gerarPdfOs(os);
            // OBS: se lá no PdfItext você chamou o metodo de gerarPdfOs ao invés de gerarOrdemServicoPdf, é só trocar o nome aqui em cima.

            System.out.println("\u001B[32m✔ PDF da OS #" + os.getId() + " gerado com sucesso na pasta do projeto!\u001B[0m");

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO AO GERAR PDF]: " + e.getMessage() + "\u001B[0m");
            e.printStackTrace();
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