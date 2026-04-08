package br.com.sosviale.service;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.repository.PassageiroRepository;
import br.com.sosviale.repository.VeiculoRepository;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class MenuService {

    // atributos para guardar os repositórios
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;

    // construtor que recebe os repositórios da main
    public MenuService(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo) {
        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
    }

    public void iniciar() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

            String prompt = new AttributedStringBuilder()
                    .append("SOS VIALE", AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN).bold())
                    .append(" > ", AttributedStyle.DEFAULT)
                    .toAnsi();

            imprimirBoasVindas();

            while (true) {
                String line = reader.readLine(prompt);
                if (line == null || line.equalsIgnoreCase("sair")) break;

                processarComando(line.trim().toLowerCase(), reader);
            }
        } catch (Exception e) {
            System.err.println("Erro na interface: " + e.getMessage());
        }
    }

    private void imprimirBoasVindas() {
        System.out.println("\u001B[36m========================================");
        System.out.println("   BEM-VINDO AO SISTEMA SOS VIALE");
        System.out.println("   (Digite 'menu' para ver as opções)");
        System.out.println("========================================\u001B[0m");
    }

    private void processarComando(String comando, LineReader reader) {
        switch (comando) {
            case "menu":
                System.out.println("\u001B[32m[1]\u001B[0m Agendar Transfer");
                System.out.println("\u001B[32m[2]\u001B[0m Cadastrar Passageiro");
                System.out.println("\u001B[32m[sair]\u001B[0m Encerra o sistema");
                break;
            case "1":
                System.out.println("Iniciando fluxo de agendamento...");
                break;
            case "2":
                cadastrarPassageiro(reader);
                break;
            default:
                System.out.println("\u001B[31mComando desconhecido.\u001B[0m");
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
}
