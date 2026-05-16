package br.com.sosviale.util;

import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.Transfer;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.time.format.DateTimeFormatter;

// Classe utilitária focada apenas na geração do arquivo PDF
public class PdfItext {

    public static String nomeArquivoPadrao(OrdemServico os) {
        String motorista = os.getMotorista() != null
                ? os.getMotorista().getNome().replaceAll("[^a-zA-Z0-9_-]", "_")
                : "sem_motorista";
        return "OS_" + os.getId() + "_" + motorista + ".pdf";
    }

    /** Gera PDF na pasta de trabalho atual (compatibilidade com chamadas antigas). */
    public static String gerarPdfOs(OrdemServico os) throws Exception {
        return gerarPdfOs(os, nomeArquivoPadrao(os));
    }

    /**
     * Gera PDF da OS no caminho informado (uso com seletor de download na UI).
     *
     * @param os              ordem de serviço com motorista, veículo e transfers carregados
     * @param caminhoDestino  caminho absoluto do arquivo .pdf
     * @return caminho do arquivo gerado
     */
    public static String gerarPdfOs(OrdemServico os, String caminhoDestino) throws Exception {
        if (os == null) throw new IllegalArgumentException("OS não pode ser nula.");
        if (caminhoDestino == null || caminhoDestino.isBlank()) {
            throw new IllegalArgumentException("Caminho do PDF inválido.");
        }
        if (os.getMotorista() == null) throw new IllegalArgumentException("OS sem motorista definido.");
        if (os.getVeiculo() == null) throw new IllegalArgumentException("OS sem veículo definido.");

        File destino = new File(caminhoDestino);
        File pai = destino.getParentFile();
        if (pai != null && !pai.exists()) pai.mkdirs();

        PdfWriter writer = new PdfWriter(destino.getAbsolutePath());
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
                document.add(new Paragraph("   Horário: " + t.getHoraTransfer().format(formatadorHora)));
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

        // fecha o documento
        document.close();

        return destino.getAbsolutePath();
    }
}