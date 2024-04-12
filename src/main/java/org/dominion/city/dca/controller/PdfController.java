package org.dominion.city.dca.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.dominion.city.dca.model.CertificateHolder;
import org.dominion.city.dca.model.User;
import org.dominion.city.dca.repository.UserRepository;
import org.dominion.city.dca.service.PdfService;
import org.dominion.city.dca.service.PdfServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PdfController {

    private final UserRepository userRepository;
    private final PdfService pdfService;
    private final PdfServiceImpl service;

    @PostConstruct
    public void PdfController(){
        if(!userRepository.existsByUsername("admin@dca.com")) {
            String sign = service.convertToBase64("src/main/resources/images/sign.png");
            userRepository.save(User.builder()
                    .chapter("Okota")
                    .username("admin@dca.com")
                    .password("90YTWGT82763TYWG@77736")
                    .pastorSign(sign)
                    .principalSign(sign)
                    .build());
        }
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/getCertificate")
    public ResponseEntity<?> getCertificate(String ref) {
        try {
            return pdfService.getCertificate(ref);
        }catch(Exception e){
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(service.sendMessage(e.getLocalizedMessage()));
        }
    }

    @PostMapping("/login")
    public String login(String username, String password) {
        Optional<User> user =  userRepository.findByUsername(username);
        if(!user.isPresent()){
            return service.sendMessage(username + " does not exist");
        }

        if(!StringUtils.equals(user.get().getPassword(), password)){
            return service.sendMessage("invalid credentials");
        }
        return "generateOption";
    }

    @PostMapping("/exportOption")
    public String exportOption(String contentSelected) {
        if(contentSelected.equalsIgnoreCase("bulk")){
            return "bulkExport";
        }else {
            return "singleExport";
        }
    }

    @SneakyThrows
    @PostMapping("/generateCertificate")
    public ResponseEntity<?> generatePdfFile(String fullName, String ref, String date) {
        try {
            CertificateHolder holder = CertificateHolder.builder()
                    .fullName(fullName)
                    .ref(ref)
                    .build();
            holder.setGraduationDate(date);
            return pdfService.generateCertificate(holder);
        }catch(Exception e){
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(service.sendMessage(e.getLocalizedMessage()));
         }
    }

    @PostMapping(value="/generateBulkCertificate",consumes = {"multipart/form-data"})
    public ResponseEntity<?> generateBulk(@RequestPart(value = "doc") MultipartFile doc){
        try {
            if (doc.getSize() <= 0) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(service.sendMessage("FILE HAS NO CONTENT"));
            }

            String contentType = Objects.requireNonNull(doc.getOriginalFilename()).substring(doc.getOriginalFilename().lastIndexOf(".") + 1);
            List<String> validType = List.of("xlsx", "xls");

            if (!validType.contains(contentType)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(service.sendMessage("INVALID FILE UPLOAD"));
            }

            ArrayList<CertificateHolder> dtoList = service.extractDataFromFile(doc);

            return pdfService.generateBulkCertificate(dtoList);
        }catch(Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(service.sendMessage(e.getLocalizedMessage()));
        }
    }

    @PostMapping("/template")
    public void getTemplate(HttpServletResponse response) {
        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dcaCertifierTemplate.csv");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(PdfServiceImpl.getHeader(List.of(new CertificateHolder()))))) {

            printer.flush();
            response.getOutputStream().write(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}