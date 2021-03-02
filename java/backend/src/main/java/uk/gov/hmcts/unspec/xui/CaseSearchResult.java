package uk.gov.hmcts.unspec.xui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jooq.generated.enums.CaseState;
import uk.gov.hmcts.ccf.XUI;
import uk.gov.hmcts.ccf.XUISearchHandler;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CaseSearchResult implements XUISearchHandler.XUISearchResult {
    @XUI(label = "Case ID")
    private Long caseId;
    @XUI(label = "Parent case ID")
    private Long parentCaseId;
    @XUI(label = "State")
    private CaseState state;

    private Long rowCount;
}
