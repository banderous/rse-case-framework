package uk.gov.hmcts.ccd.domain.model.aggregated;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;

import java.util.List;

@Builder
@Data
public class JurisdictionDisplayProperties {
    private String id;
    private String name;
    private String description;

    @Singular
    private List<CaseTypeDefinition> caseTypes;

}
