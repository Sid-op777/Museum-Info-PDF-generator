package com.museumadmin.pdf_generator.controller;


import com.museumadmin.pdf_generator.service.PdfGeneratorService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

@RestController
//@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @PostMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestBody Map<String, Object> request) {
        try {
            JSONObject museumData = new JSONObject(request);
            byte[] pdfBytes = pdfGeneratorService.createPdf(museumData);

            return createPdfResponse(pdfBytes);
        } catch (IOException | JSONException e) {
            return handlePdfGenerationError(e);
        }
    }

    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "museum_information.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private ResponseEntity<byte[]> handlePdfGenerationError(Exception e) {
        // Log the error (optional)
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
