package uk.gov.hmcts.unspec.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.unspec.dto.LegalRepresentative;
import uk.gov.hmcts.unspec.dto.Party;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class CreateClaim {
    @NonNull
    private String claimantReference;
    @NonNull
    private String defendantReference;

    private String applicantPreferredCourt;

    private Party claimant;
    private Party defendant;

    private String defendantLegalRepEmail;
    private LegalRepresentative defendantLegalRep;

    private String claimType;
    private String claimSubType;
    private String claimDetails;
    private String claimParticulars;

    private long lowerValue;
    private long higherValue;

    private String feeAccountNumber;

    private String statementOfTruth;

}
