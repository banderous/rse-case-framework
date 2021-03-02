package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;


@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WizardPageField implements Serializable {

    private String caseFieldId;
    private Integer order;
    private Integer pageColumnNumber;
    private List<WizardPageComplexFieldOverride> complexFieldOverrides;

    @JsonProperty("case_field_id")
    public String getCaseFieldId() {
        return caseFieldId;
    }

    public void setCaseFieldId(String caseFieldId) {
        this.caseFieldId = caseFieldId;
    }

    @JsonProperty("order")
    public int getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @JsonProperty("page_column_no")
    public Integer getPageColumnNumber() {
        return pageColumnNumber;
    }

    public void setPageColumnNumber(Integer number) {
        this.pageColumnNumber = number;
    }

    @JsonProperty("complex_field_overrides")
    public List<WizardPageComplexFieldOverride> getComplexFieldOverrides() {
        return complexFieldOverrides;
    }

    public void setComplexFieldOverrides(List<WizardPageComplexFieldOverride> complexFieldOverrides) {
        this.complexFieldOverrides = complexFieldOverrides;
    }

    public Optional<WizardPageComplexFieldOverride> getComplexFieldOverride(String fieldPath) {
        return getComplexFieldOverrides().stream()
            .filter(override -> fieldPath.equals(override.getComplexFieldElementId()))
            .findAny();
    }
}
