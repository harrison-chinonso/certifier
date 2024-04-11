package org.dominion.city.dca.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateHolder {
    public String fullName;
    public String ref;
    public String graduationDate;

    public void setGraduationDate(String graduationDate) {
        LocalDate dates = LocalDate.of(Integer.parseInt(graduationDate.split("-")[0]),Integer.parseInt(graduationDate.split("-")[1]), 3);
        this.graduationDate = dates.getMonth().name()+" "+dates.getYear();
    }
}
