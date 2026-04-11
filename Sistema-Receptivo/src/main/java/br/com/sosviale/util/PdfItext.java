package br.com.sosviale.util;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.Transfer;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.time.format.DateTimeFormatter;

public class PdfItext {

    /**
     * Recebe a OS montada e gera um PDF com todos os dados da rota.
     * Nome do arquivo gerado: OS_<id>_<nome_motorista>.pdf
     */
    public static void gerarPdfOs(OrdemServico os) throws Exception {

        // 1. Nome do arquivo dinâmico (ex: OS_5_Joao_Silva.pdf)
        String path = "OS_" + os.getId() + "_" + os.getMotorista().getNome().replace(" ", "_") + ".pdf";

        // 2. Inicializa o Writer e o PdfDocument
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);

        // 3. Cria o documento de alto nível
        Document document = new Document(pdf);

        // Formatadores de data e hora para exibição no documento
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatadorHora = DateTimeFormatter.ofPattern("HH:mm");

        // 4. Cabeçalho da OS
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

        // 5. Lista de transfers ordenados da OS
        if (os.getTransfers().isEmpty()) {
            document.add(new Paragraph("Nenhum transfer atribuído a esta OS."));
        } else {
            for (int i = 0; i < os.getTransfers().size(); i++) {
                Transfer t = os.getTransfers().get(i);

                document.add(new Paragraph("-> PARADA " + (i + 1) + " (Transfer #" + t.getId() + ")"));
                document.add(new Paragraph("   Horário: " + t.getDataHora().format(formatadorHora)));
                document.add(new Paragraph("   De: " + t.getOrigem() + "  |  Para: " + t.getDestino()));

                // Monta a lista de passageiros deste transfer
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

        // 6. Rodapé e assinatura
        document.add(new Paragraph("======================================================"));
        document.add(new Paragraph(""));
        document.add(new Paragraph(""));
        document.add(new Paragraph("Assinatura do Motorista: _______________________________"));

        // 7. Fecha o documento (obrigatório para gravar o arquivo)
        document.close();
    }
}
