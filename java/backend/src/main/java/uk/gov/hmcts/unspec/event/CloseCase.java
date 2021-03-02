package uk.gov.hmcts.unspec.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccf.XUI;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloseCase {
    @XUI(label = "Reason for closure")
    private String reason;
}
