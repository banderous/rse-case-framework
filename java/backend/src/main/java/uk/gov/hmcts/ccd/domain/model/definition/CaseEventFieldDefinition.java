package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import uk.gov.hmcts.ccd.domain.model.common.CommonDCPModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ToString

public class CaseEventFieldDefinition implements Serializable, CommonDCPModel {

    private String caseFieldId = null;
    private String displayContext = null;
    private String displayContextParameter = null;
    private String showCondition = null;
    private Boolean showSummaryChangeOption = null;
    private Integer showSummaryContentOption = null;
    private String label = null;
    private String hintText = null;
    private Boolean retainHiddenValue;
    private String defaultValue;
    private List<CaseEventFieldComplexDefinition> caseEventFieldComplexDefinitions = new ArrayList<>();


    @JsonProperty("case_field_id")
    public String getCaseFieldId() {
        return caseFieldId;
    }

    public void setCaseFieldId(String caseFieldId) {
        this.caseFieldId = caseFieldId;
    }


    @JsonProperty("display_context")
    public String getDisplayContext() {
        return displayContext;
    }

    public void setDisplayContext(String displayContext) {
        this.displayContext = displayContext;
    }


    @JsonProperty("display_context_parameter")
    public String getDisplayContextParameter() {
        return displayContextParameter;
    }

    public void setDisplayContextParameter(String displayContextParameter) {
        this.displayContextParameter = displayContextParameter;
    }


    @JsonProperty("show_condition")
    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }


    @JsonProperty("show_summary_change_option")
    public Boolean getShowSummaryChangeOption() {
        return showSummaryChangeOption;
    }

    public void setShowSummaryChangeOption(final Boolean showSummaryChangeOption) {
        this.showSummaryChangeOption = showSummaryChangeOption;
    }


    @JsonProperty("show_summary_content_option")
    public Integer getShowSummaryContentOption() {
        return showSummaryContentOption;
    }

    public void setShowSummaryContentOption(Integer showSummaryContentOption) {
        this.showSummaryContentOption = showSummaryContentOption;
    }

    /**
     * event case field label.
     **/

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * event case field hint text.
     **/

    @JsonProperty("hint_text")
    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }


    @JsonProperty("case_fields_complex")
    public List<CaseEventFieldComplexDefinition> getCaseEventFieldComplexDefinitions() {
        return caseEventFieldComplexDefinitions;
    }

    public void setCaseEventFieldComplexDefinitions(List<CaseEventFieldComplexDefinition> eventComplexTypeEntities) {
        this.caseEventFieldComplexDefinitions = eventComplexTypeEntities;
    }


    @JsonProperty("defaultValue")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    @JsonProperty("retain_hidden_value")
    public Boolean getRetainHiddenValue() {
        return retainHiddenValue;
    }

    public void setRetainHiddenValue(Boolean retainHiddenValue) {
        this.retainHiddenValue = retainHiddenValue;
    }

}
