package uk.gov.hmcts.unspec.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Individual extends Party {
    private String title;
    private String firstName;
    private String lastName;
    // TODO
    @JsonIgnore
    private LocalDate dateOfBirth;

    @Override
    public String name() {
        return title + " " + firstName + " " + lastName;
    }
}
