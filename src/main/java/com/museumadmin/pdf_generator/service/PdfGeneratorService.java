package com.museumadmin.pdf_generator.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    public byte[] createPdf(String title, String content) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph(title).setFontSize(20));
            document.add(new Paragraph(content));

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
}