package br.com.sosviale.util;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.Transfer;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.time.format.DateTimeFormatter;

// utilitária para geração de PDF de Ordens de Serviço usando a biblioteca iText
public class PdfItext {

    /**
     * Gera um arquivo PDF com os dados completos da OS informada.
     * O arquivo é salvo na pasta raiz do projeto com o nome: OS_<id>_<nome_motorista>.pdf
     *
     * @param os a ordem de serviço a ser exportada (não pode ser nula)
     * @throws IllegalArgumentException se a OS ou seus dados obrigatórios estiverem nulos
     * @throws Exception se ocorrer falha ao criar ou gravar o arquivo PDF
     */
    public static void gerarPdfOs(OrdemServico os) throws Exception {
        if (os == null) throw new IllegalArgumentException("OS não pode ser nula.");
        if (os.getMotorista() == null) throw new IllegalArgumentException("OS sem motorista definido.");
        if (os.getVeiculo() == null) throw new IllegalArgumentException("OS sem veículo definido.");

        // nome do arquivo gerado dinamicamente com base no ID e nome do motorista
        String path = "OS_" + os.getId() + "_" + os.getMotorista().getNome().replace(" ", "_") + ".pdf";

        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatadorHora = DateTimeFormatter.ofPattern("HH:mm");

        // cabeçalho da OS
        document.add(new Paragraph("======================================================"));
        document.add(new Paragraph("             ORDEM DE SERVIÇO - SOS VIALE             "));
        document.add(new Paragraph("======================================================"));
        document.add(new Paragraph("OS Nº: " + os.getId()));
        document.add(new Paragraph("Data do Serviço: " + os.getDataServico().format(formatadorData)));
        document.add(new Paragraph("Motorista: " + os.getMotorista().getNome() + " (CNH: " + os.getMotorista().getCnh() + ")"));
        document.add(new Paragraph("Veículo: " + os.getVeiculo().getLabel() + " (Placa: " + os.getVeiculo().getPlaca() + ")"));
        document.add(new Paragraph("Capacidade Máxima: " + os.getVeiculo().getCapacidade() + " passageiros"));
        document.add(new Paragraph("------------------------------------------------------"));
        document.add(new Paragraph("ROTA DE TRANSFERS AGENDADOS:"));
        document.add(new Paragraph(""));

        // lista de transfers ordenados da OS
        if (os.getTransfers().isEmpty()) {
            document.add(new Paragraph("Nenhum transfer atribuído a esta OS."));
        } else {
            for (int i = 0; i < os.getTransfers().size(); i++) {
                Transfer t = os.getTransfers().get(i);

                document.add(new Paragraph("-> PARADA " + (i + 1) + " (Transfer #" + t.getId() + ")"));
                document.add(new Paragraph("   Horário: " + t.getDataHora().format(formatadorHora)));
                document.add(new Paragraph("   De: " + t.getOrigem() + "  |  Para: " + t.getDestino()));

                // monta a lista de nomes dos passageiros do transfer
                StringBuilder nomesPax = new StringBuilder();
                for (Passageiro p : t.getPassageiros()) {
                    nomesPax.append(p.getNome()).append(", ");
                }
                String listaPax = nomesPax.length() > 0
                        ? nomesPax.substring(0, nomesPax.length() - 2)
                        : "Nenhum";

                document.add(new Paragraph("   Passageiros (" + t.getPassageiros().size() + "): " + listaPax));
                document.add(new Paragraph(""));
            }
        }

        // rodapé com campo de assinatura do motorista
        document.add(new Paragraph("======================================================"));
        document.add(new Paragraph(""));
        document.add(new Paragraph(""));
        document.add(new Paragraph("Assinatura do Motorista: _______________________________"));

        // fecha o documento — obrigatório para gravar os bytes no arquivo
        document.close();
    }
}
