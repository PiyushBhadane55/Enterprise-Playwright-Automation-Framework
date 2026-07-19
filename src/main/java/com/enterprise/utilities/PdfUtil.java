package com.enterprise.utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * PDF Utility class to parse and extract text content from PDF documents for validation.
 */
public class PdfUtil {

    private static final Logger log = LogManager.getLogger(PdfUtil.class);

    /**
     * Extracts all text content from a PDF document.
     *
     * @param filePath Path to the PDF file
     * @return Extracted text content
     */
    public static String getPdfText(String filePath) {
        log.info("Extracting text from PDF file: {}", filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException("PDF file does not exist: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Successfully extracted {} characters from PDF.", text.length());
            return text;
        } catch (IOException e) {
            log.error("Failed to parse PDF document.", e);
            throw new RuntimeException("PDF extraction failure.", e);
        }
    }

    /**
     * Retrieves the total page count of the PDF document.
     */
    public static int getPageCount(String filePath) {
        log.info("Reading page count of PDF file: {}", filePath);
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            int pageCount = document.getNumberOfPages();
            log.info("PDF page count: {}", pageCount);
            return pageCount;
        } catch (IOException e) {
            log.error("Failed to read page count of PDF document.", e);
            throw new RuntimeException("PDF metadata reading failure.", e);
        }
    }

    /**
     * Dynamically generates a test PDF document for download validation.
     */
    public static void createSamplePdf(String outputPath, String customerName) {
        log.info("Generating sample PDF report at: {}", outputPath);
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("ENTERPRISE TEST SYSTEM CUSTOMER REPORT");
                
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("Customer Name: " + customerName);
                
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Status: ACTIVE");
                
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Verification Code: 987654");
                
                contentStream.endText();
            }

            File outputFile = new File(outputPath);
            if (outputFile.getParentFile() != null) {
                outputFile.getParentFile().mkdirs();
            }
            document.save(outputFile);
            log.info("Sample PDF report saved successfully.");
        } catch (IOException e) {
            log.error("Failed to create sample PDF report.", e);
        }
    }
}
