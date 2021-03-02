package uk.gov.hmcts.unspec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccf.XUI;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddParty {

    @XUI(label = "Type of party")
    private PartyType partyType;

    @XUI(label = "Title")
    private String title;

    @XUI(label = "Forename")
    private String firstName;

    @XUI(label = "Surname")
    private String lastName;

    @XUI(label = "Date of birth")
    private LocalDate dateOfBirth;

    @XUI(label = "Name")
    private String name;
}
