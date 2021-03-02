package uk.gov.hmcts.unspec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccf.XUI;
import uk.gov.hmcts.ccf.XUIType;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddClaim {
    @XUI(label = "Select claimants")
    private Set<Long> claimants;
    @XUI(label = "Select defendants")
    private Set<Long> defendants;
    @XUI(label = "Claim lower value", min = 0, type = XUIType.MoneyGBP)
    private long lowerValue;
    @XUI(label = "Claim higher value", type = XUIType.MoneyGBP)
    private long higherValue;
}
