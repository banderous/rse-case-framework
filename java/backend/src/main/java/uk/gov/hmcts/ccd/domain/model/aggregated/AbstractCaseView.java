package uk.gov.hmcts.ccd.domain.model.aggregated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.ToString;

import java.util.List;

@ToString
public abstract class AbstractCaseView {

    @JsonProperty("case_id")
    private long caseId;
    @JsonProperty("case_type")
    private CaseViewType caseType;
    private List<CaseViewTab> tabs;
    private List<CaseViewField> metadataFields = Lists.newArrayList();

    public long getCaseId() {
        return caseId;
    }

    public void setCaseId(long caseId) {
        this.caseId = caseId;
    }

    public CaseViewType getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseViewType caseType) {
        this.caseType = caseType;
    }

    public List<CaseViewTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<CaseViewTab> tabs) {
        this.tabs = tabs;
    }

    public List<CaseViewField> getMetadataFields() {
        return metadataFields;
    }

    public void setMetadataFields(List<CaseViewField> metadataFields) {
        this.metadataFields = metadataFields;
    }
}
