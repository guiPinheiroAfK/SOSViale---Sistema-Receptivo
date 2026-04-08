package br.com.sosviale.service;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

public class MenuService {

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

                processarComando(line.trim().toLowerCase());
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

    private void processarComando(String comando) {
        switch (comando) {
            case "menu":
                System.out.println("\u001B[32m[1]\u001B[0m Agendar Transfer");
                System.out.println("\u001B[32m[2]\u001B[0m Cadastrar Passageiro");
                System.out.println("\u001B[32m[sair]\u001B[0m Encerra o sistema");
                break;
            case "1":
                System.out.println("Iniciando fluxo de agendamento...");
                break;
            default:
                System.out.println("\u001B[31mComando desconhecido.\u001B[0m");
        }
    }
}
