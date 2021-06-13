package uk.gov.hmcts.ccd.v2.internal.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseHistoryView;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTab;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewType;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CaseHistoryViewResource extends RepresentationModel {

    @JsonProperty("case_id")
    private long caseId;
    @JsonProperty("case_type")
    private CaseViewType caseType;
    private List<CaseViewTab> tabs;
    private List<CaseViewField> metadataFields;
    @JsonProperty("event")
    private CaseViewEvent event;

    public CaseHistoryViewResource(@NonNull CaseHistoryView caseHistoryView, String caseId) {
        copyProperties(caseHistoryView);
    }

    private void copyProperties(CaseHistoryView caseViewEvent) {
        this.caseId = caseViewEvent.getCaseId();
        this.caseType = caseViewEvent.getCaseType();
        this.tabs = caseViewEvent.getTabs();
        this.metadataFields = caseViewEvent.getMetadataFields();
        this.event = caseViewEvent.getEvent();
    }
}
