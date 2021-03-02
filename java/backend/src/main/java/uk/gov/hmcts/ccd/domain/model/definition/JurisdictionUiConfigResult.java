package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("checkstyle:SummaryJavadoc")

public class JurisdictionUiConfigResult implements Serializable {

    private List<JurisdictionUiConfigDefinition> configs;

    public JurisdictionUiConfigResult() {
    }

    public JurisdictionUiConfigResult(List<JurisdictionUiConfigDefinition> configs) {
        this.configs = configs;
    }

    /**
     *
     **/

    @JsonProperty("configs")
    public List<JurisdictionUiConfigDefinition> getConfigs() {
        return configs;
    }

    public void setConfigs(List<JurisdictionUiConfigDefinition> configs) {
        this.configs = configs;
    }

}
