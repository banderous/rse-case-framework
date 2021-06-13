package uk.gov.hmcts.ccd.v2.internal.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseView;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewActionableEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTab;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewType;
import uk.gov.hmcts.ccd.domain.model.aggregated.ProfileCaseState;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CaseViewResource extends RepresentationModel {

    @JsonProperty("case_id")
    private String reference;

    @JsonProperty("case_type")
    private CaseViewType caseType;

    @JsonProperty("tabs")
    private List<CaseViewTab> tabs;

    @JsonProperty("metadataFields")
    private List<CaseViewField> metadataFields = Lists.newArrayList();

    @JsonProperty("state")
    private ProfileCaseState state;

    @JsonProperty("triggers")
    private List<CaseViewActionableEvent> caseViewActionableEvents = Lists.newArrayList();

    @JsonProperty("events")
    private CaseViewEvent[] caseViewEvents;

    public CaseViewResource(@NonNull CaseView caseView) {
        copyProperties(caseView);

    }

    private void copyProperties(CaseView caseView) {
        this.reference = String.valueOf(caseView.getCaseId());
        this.caseType = caseView.getCaseType();
        this.tabs = caseView.getTabs();
        this.metadataFields = caseView.getMetadataFields();
        this.state = caseView.getState();
        this.caseViewActionableEvents = caseView.getActionableEvents();
        this.caseViewEvents = caseView.getEvents();
    }
}
