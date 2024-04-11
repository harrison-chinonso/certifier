package org.dominion.city.dca.service;

import io.github.millij.poi.ss.reader.SpreadsheetReader;
import io.github.millij.poi.ss.reader.XlsReader;
import io.github.millij.poi.ss.reader.XlsxReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.dominion.city.dca.exceptions.BadRequestException;
import org.dominion.city.dca.model.CertificateHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final TemplateEngine templateEngine;
    @SneakyThrows
    @Override
    public ResponseEntity<?> generateCertificate(CertificateHolder holder){
        return exportPdf(List.of(holder));
    }

    @SneakyThrows
    @Override
    public ResponseEntity<?> generateBulkCertificate(List<CertificateHolder> holders){
        return exportPdf(holders);
    }
//th:src="${logo}"
    private String parseThymeleafTemplate(CertificateHolder detail) {
        String logo = convertToBase64("src/main/resources/images/logo.png");
        String sign = convertToBase64("src/main/resources/images/sign.png");
        Map<String, Object> emailParams = Map.of(
            "name", detail.getFullName().toUpperCase(),
            "date", detail.getGraduationDate(),
            "ref", detail.getRef(),
            "logo", logo,
            "sign", sign
        );

        Context context = new Context();
        context.setVariables(emailParams);

        return templateEngine.process("pdf", context);
    }
    @SneakyThrows
    public ByteArrayOutputStream convertHtmlToPdf(CertificateHolder holder) {
        String processedHtml = parseThymeleafTemplate(holder);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(processedHtml);
        renderer.layout();
        renderer.createPDF(outputStream, false);
        renderer.finishPDF();
        return outputStream;
    }
    public List<byte[]> generateMultiplePdfFiles(List<CertificateHolder> details){
        List<byte[]> result = new ArrayList<>();
        for(CertificateHolder detail : details){
           result.add(convertHtmlToPdf(detail).toByteArray());
        }
        return result;
    }
    public ResponseEntity<?> exportPdf(List<CertificateHolder> details) throws IOException {
        // Generate multiple PDF files (replace this with your logic)
        List<byte[]> pdfBytesList = generateMultiplePdfFiles(details);

        // Create a ZIP file containing the PDF files
        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(zipOutputStream)) {
            for (int i = 0; i < pdfBytesList.size(); i++) {
                byte[] pdfBytes = pdfBytesList.get(i);
                ZipEntry entry = new ZipEntry(details.get(i).getFullName() + ".pdf");
                zip.putNextEntry(entry);
                zip.write(pdfBytes);
                zip.closeEntry();
            }
        }

        // Prepare response with ZIP file for download
        byte[] zipBytes = zipOutputStream.toByteArray();
        InputStreamResource pdfStream = new InputStreamResource(new ByteArrayInputStream(zipBytes));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DCA-Certificates.zip");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        headers.setContentLength(zipBytes.length);
log.info("DONE");
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(pdfStream);
    }
    public static <O> String[] getHeader(Collection<O> objects) {
        // Retrieve header from the first element's fields
        O firstObject = objects.stream().findFirst().orElseThrow(IllegalArgumentException::new);
        Field[] fields = firstObject.getClass().getDeclaredFields();
        String[] header = new String[fields.length];

        for (int i = 0; i < fields.length; i++) header[i] = fields[i].getName();

        return header;
    }
    public ArrayList<CertificateHolder> extractDataFromFile(MultipartFile doc) {
        ArrayList<CertificateHolder> dtoList = new ArrayList<>();
        String ext = FilenameUtils.getExtension(doc.getOriginalFilename());
        try (InputStream file = doc.getInputStream()) {
            final SpreadsheetReader reader = "xlsx".equalsIgnoreCase(ext) ? new XlsxReader() : new XlsReader();
            dtoList.addAll(reader.read(CertificateHolder.class, file) );

            return dtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("There was an error parsing uploaded file : "+ e.getMessage());
        }
    }
    public String sendMessage(String message){
        return "<html xmlns:th=\"https://www.thymeleaf.org\">\n" +
                "<head>\n" +
                "    <title>DOMINION CITY ACADEMY</title>\n" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "    <div class=\"alert alert-primary\" role=\"alert\">\n" +
                "        <p>"+message+"</p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
    private String convertToBase64(String filePath) {
        Path path = Paths.get(filePath);
        byte[] imageAsBytes = new byte[0];
        try {
            Resource resource = new UrlResource(path.toUri());
            InputStream inputStream = resource.getInputStream();
            imageAsBytes = IOUtils.toByteArray(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\n File read Exception");
        }

        return Base64.getEncoder().encodeToString(imageAsBytes);
    }

//    public void generatePdfFromHtml(String html) throws IOException, DocumentException {
//        String outputFolder = "certificate.pdf";
//        FileOutputStream fileOutputStream = new FileOutputStream(outputFolder);
//        ITextRenderer renderer = new ITextRenderer();
//        renderer.setDocumentFromString(html);
//        renderer.layout();
//        renderer.createPDF(fileOutputStream, false);
//        renderer.finishPDF();
//    }
//
//    @SneakyThrows
//    public void convertHtmlToPdf2() {
//        String processedHtml = parseThymeleafTemplate("");
//        FileOutputStream os = null;
//        String fileNameUrl = "";
//        try {
//            final File outputFile = File.createTempFile("Student_01"+"_", ".pdf");
//
//            os = new FileOutputStream(outputFile);
//            ITextRenderer itr = new ITextRenderer();
//
//            itr.setDocumentFromString(processedHtml);
//            itr.layout();
//            itr.createPDF(os, false);
//            itr.finishPDF();
//            fileNameUrl = outputFile.getName();
//        } finally {
//            if (os != null) {
//                try {os.close();} catch (IOException ignored) { }
//            }
//        }
//
//        log.info("fileNameUrl {}", fileNameUrl);
//
//    }

}