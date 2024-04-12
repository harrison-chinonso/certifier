package org.dominion.city.dca.service;

import lombok.SneakyThrows;
import org.dominion.city.dca.model.CertificateHolder;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PdfService {
    @SneakyThrows
    ResponseEntity<?> generateBulkCertificate(List<CertificateHolder> holders);

    @SneakyThrows
    ResponseEntity<?> generateCertificate(CertificateHolder holder);

    @SneakyThrows
    ResponseEntity<?> getCertificate(String ref);
}