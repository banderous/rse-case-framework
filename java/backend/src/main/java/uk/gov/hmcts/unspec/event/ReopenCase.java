package uk.gov.hmcts.unspec.event;

import lombok.Data;
import uk.gov.hmcts.ccf.XUI;

@Data
public class ReopenCase {
    @XUI(label = "Reason for reopening")
    private String reason;
}
