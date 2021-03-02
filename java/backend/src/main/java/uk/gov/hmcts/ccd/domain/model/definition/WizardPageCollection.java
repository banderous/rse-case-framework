package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;


public class WizardPageCollection {

    private String caseTypeId = null;
    private String eventId = null;
    private List<WizardPage> wizardPages = new ArrayList<>();

    public WizardPageCollection() {
    }

    public WizardPageCollection(String caseTypeId, String eventId) {
        this.caseTypeId = caseTypeId;
        this.eventId = eventId;
    }

    /**
     * Unique identifier for a Case Type.
     **/

    @JsonProperty("case_type_id")
    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    /**
     * Identifier for the Event that is related to CaseTypeId.
     **/

    @JsonProperty("event_id")
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


    @JsonProperty("wizard_pages")
    public List<WizardPage> getWizardPages() {
        return wizardPages;
    }

    public void setTabs(List<WizardPage> wizardPages) {
        this.wizardPages = wizardPages;
    }
}

