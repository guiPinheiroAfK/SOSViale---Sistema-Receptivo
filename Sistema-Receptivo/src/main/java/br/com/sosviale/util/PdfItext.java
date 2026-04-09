package br.com.sosviale.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import java.io.FileNotFoundException;

public class PdfItext {
    public static void main(String[] args) {
        // 1. caminho onde o PDF será salvo
        String path = "Relatorio_AutoHub.pdf";

        try {
            // 2. inicicializa o Writer e o PdfDocument
            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdf = new PdfDocument(writer);

            // 3. cria o documento de alto nível para adicionar elementos
            Document document = new Document(pdf);

            // 4. adiciona conteúdo
            document.add(new Paragraph("Olá, Gui!"));
            document.add(new Paragraph("Este é um PDF gerado via iText 7 no projeto SOSViale - Receptivo."));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph("to testando se a linha pula"));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph("krl pula msm pqp"));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph("como eu sei q linha eu to?"));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph(""));
            document.add(new Paragraph("eu não sei."));

            // 5. fecha o documento (importante!)
            document.close();

            System.out.println("PDF gerado com sucesso em: " + path);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}