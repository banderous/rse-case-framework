package uk.gov.hmcts.ccd.v2.internal.resource;

import lombok.Builder;
import lombok.Singular;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;

import java.util.List;

@Builder
public class JurisdictionViewResource extends RepresentationModel {

    @Singular
    private List<JurisdictionView> jurisdictions;

    @Builder
    public static class JurisdictionView {
        private String id;
        private String name;
        private String description;
        @Singular
        private List<CaseTypeDefinition> caseTypeDefinitions;
    }
}
