package uk.gov.hmcts.ccf.types.dto;

import lombok.Data;
import uk.gov.hmcts.ccf.XUI;

import java.time.LocalDate;
import java.util.Set;

@Data
public class GeneralReferral {
    ReferralReason reason;
    @XUI(label = "Application or referral date")
    LocalDate date;
    String referralDetails;
    Set<Integer> ids;
    boolean feeRequired;
}
