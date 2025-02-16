package com.museumadmin.pdf_generator.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfGeneratorService {

    public byte[] createPdf(JSONObject museum) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            JSONObject museumData = museum.getJSONObject("museum");
            float margin = 50;
            float availableWidth = 595 - margin * 2; // Assuming A4 width
            float yPosition = 750; // Start position

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                //DOES NOT HANDLE MULTIPLE PAGES!

                // Title Section
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.showText("Museum ID: " + museumData.getString("id"));
                contentStream.endText();

                yPosition -= 20; // Move down for the next line

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.showText("Museum Name: " + museumData.getString("name"));
                contentStream.endText();
                yPosition -= 30; // Space before the description

                // Description Section
                drawWrappedText(contentStream, museumData.getString("description"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                yPosition -= (museumData.getString("description").length() / 60 * 15) + 30; // Estimate height based on length

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Location:");
                contentStream.endText();

                yPosition -= 15;

                String address = museumData.getJSONObject("location").getJSONObject("address").getString("street") + ", " +
                        museumData.getJSONObject("location").getJSONObject("address").getString("city") + ", " +
                        museumData.getJSONObject("location").getJSONObject("address").getString("state") + " " +
                        museumData.getJSONObject("location").getJSONObject("address").getString("postalCode");

                drawWrappedText(contentStream, address, margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= (address.length() / 60 * 15) + 20;

                String latitude = museumData.getJSONObject("location").getString("latitude");
                String longitude = museumData.getJSONObject("location").getString("longitude");

// Create Google Maps URL
                String googleMapsLink = "https://www.google.com/maps?q=" + latitude + "," + longitude;

// Add Google Maps link to the PDF
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.showText("Google Maps Link: " + googleMapsLink);
                contentStream.endText();
                yPosition -= 15; // Adjust yPosition accordingly

                // Contact Information
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Contact Information:");
                contentStream.endText();

                yPosition -= 15; // Move down
                JSONObject contact = museumData.getJSONObject("contact");
                drawWrappedText(contentStream, "Phone: " + contact.getString("phone"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 15;

                drawWrappedText(contentStream, "Email: " + contact.getString("email"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 15;

                drawWrappedText(contentStream, "Website: " + contact.getString("website"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 30; // Space before operating hours

                // Operating Hours Section
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Operating Hours:");
                contentStream.endText();

                yPosition -= 15; // Move down
                JSONObject operatingHours = museumData.getJSONObject("operating").getJSONObject("days");
                for (String day : operatingHours.keySet()) {
                    drawWrappedText(contentStream, day.substring(0, 1).toUpperCase() + day.substring(1) + ": " + operatingHours.getString(day), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    yPosition -= 15;
                }

                // Special Operating Hours
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Special Operating Hours:");
                contentStream.endText();

                yPosition -= 15; // Move down
                for (var special : museumData.getJSONObject("operating").getJSONArray("special")) {
                    JSONObject specialInfo = (JSONObject) special;
                    String specialText = "Date: " + specialInfo.getString("date") + ", " + specialInfo.getString("description");
                    drawWrappedText(contentStream, specialText, margin, yPosition, availableWidth,new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    yPosition -= 15;
                }

                yPosition -= 20; // Space before ticket info

                // Ticket Information Section
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Ticket Information:");
                contentStream.endText();

                yPosition -= 15; // Move down
                for (var ticket : museumData.getJSONArray("ticket")) {
                    JSONObject ticketInfo = (JSONObject) ticket;
                    drawWrappedText(contentStream, "Ticket Type: " + ticketInfo.getString("type"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    yPosition -= 15;

                    // Adult Prices
                    JSONObject adultPrices = ticketInfo.getJSONArray("adult").getJSONObject(0);
                    drawWrappedText(contentStream, "Adult - Weekday: " + adultPrices.getString("weekday") + ", Weekend: " + adultPrices.getString("weekend"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    yPosition -= 15;

                    // Children Prices
                    JSONObject childrenPrices = ticketInfo.getJSONArray("children").getJSONObject(0);
                    drawWrappedText(contentStream, "Children - Weekday: " + childrenPrices.getString("weekday") + ", Weekend: " + childrenPrices.getString("weekend"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    yPosition -= 20; // Space after ticket info
                }

                // Group Ticket Information
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Group Ticket Information:");
                contentStream.endText();

                yPosition -= 15; // Move down
                drawWrappedText(contentStream, museumData.getString("groupTicket"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= (museumData.getString("groupTicket").length() / 60 * 15) + 20; // Estimate height

                // Social Media Section
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Social Media:");
                contentStream.endText();

                yPosition -= 15; // Move down
                JSONObject socialMedia = museumData.getJSONObject("socialMedia");
                drawWrappedText(contentStream, "Facebook: " + socialMedia.getString("facebook"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 15;

                drawWrappedText(contentStream, "Twitter: " + socialMedia.getString("twitter"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 15;

                drawWrappedText(contentStream, "Instagram: " + socialMedia.getString("instagram"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 20; // Space before accessibility

                // Accessibility Section
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.showText("Accessibility Features:");
                contentStream.endText();

                yPosition -= 15; // Move down
                JSONObject accessibility = museumData.getJSONObject("accessibility");
                drawWrappedText(contentStream, "Wheelchair Accessible: " + accessibility.getString("wheelchairAccessible"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 15;

                drawWrappedText(contentStream, "Assistance Available: " + accessibility.getString("assistanceAvailable"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 15;

                drawWrappedText(contentStream, "Parking: " + accessibility.getString("parking"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 15;

                drawWrappedText(contentStream, "Other Features: " + accessibility.getString("otherFeatures"), margin, yPosition, availableWidth, new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                yPosition -= 30; // Final space



            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void drawWrappedText(PDPageContentStream contentStream, String text, float startX, float startY, float maxWidth, PDType1Font font, float fontSize) throws IOException {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float lineHeight = fontSize * 1.2f; // Adjust line height if needed

        for (String word : words) {
            String testLine = line.append(word).append(" ").toString();
            float testWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (testWidth > maxWidth) {
                // If the line exceeds the width, draw the line and start a new one
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset(startX, startY);
                contentStream.showText(line.toString().trim());
                contentStream.endText();

                // Start a new line
                line = new StringBuilder(word + " ");
                startY -= lineHeight; // Move down for the next line
            }
        }

        // Draw any remaining text
        if (line.length() > 0) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(startX, startY);
            contentStream.showText(line.toString().trim());
            contentStream.endText();
        }
    }


}
