package uk.gov.hmcts.ccd.domain.model.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@ToString
public class JurisdictionDefinition implements Serializable {

    private String id = null;
    private String name = null;
    private String description = null;
    private Date liveFrom = null;
    private Date liveUntil = null;

    private List<CaseTypeDefinition> caseTypeDefinitions = new ArrayList<>();


    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @JsonProperty("live_from")
    public Date getLiveFrom() {
        return liveFrom;
    }

    public void setLiveFrom(Date liveFrom) {
        this.liveFrom = liveFrom;
    }


    @JsonProperty("live_until")
    public Date getLiveUntil() {
        return liveUntil;
    }

    public void setLiveUntil(Date liveUntil) {
        this.liveUntil = liveUntil;
    }


    @JsonProperty("case_types")
    public List<CaseTypeDefinition> getCaseTypeDefinitions() {
        return caseTypeDefinitions;
    }

    public void setCaseTypeDefinitions(List<CaseTypeDefinition> caseTypeDefinitions) {
        this.caseTypeDefinitions = caseTypeDefinitions;
    }

    public List<String> getCaseTypesIDs() {
        return this.getCaseTypeDefinitions().stream().map(CaseTypeDefinition::getId).collect(Collectors.toList());
    }
}
