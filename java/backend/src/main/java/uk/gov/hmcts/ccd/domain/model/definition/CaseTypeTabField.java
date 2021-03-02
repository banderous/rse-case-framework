package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.ccd.domain.model.common.CommonDCPModel;

import java.io.Serializable;


public class CaseTypeTabField implements Serializable, CommonDCPModel {
    private CaseFieldDefinition caseFieldDefinition = null;
    private Integer displayOrder = null;
    private String showCondition = null;
    private String displayContextParameter;


    @JsonProperty("case_field")
    public CaseFieldDefinition getCaseFieldDefinition() {
        return caseFieldDefinition;
    }

    public void setCaseFieldDefinition(final CaseFieldDefinition caseFieldDefinition) {
        this.caseFieldDefinition = caseFieldDefinition;
    }


    @JsonProperty("order")
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(final Integer displayOrder) {
        this.displayOrder = displayOrder;
    }


    @JsonProperty("show_condition")
    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }


    @JsonProperty("display_context_parameter")
    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }
}
