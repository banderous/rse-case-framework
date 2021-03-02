package uk.gov.hmcts.ccd.v2.internal.resource;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.definition.JurisdictionUiConfigDefinition;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class JurisdictionConfigViewResource extends RepresentationModel {

    private List<JurisdictionUiConfigDefinition> configs;

    public JurisdictionConfigViewResource(@NonNull List<JurisdictionUiConfigDefinition> listOfConfigs) {
        copyProperties(listOfConfigs);
    }

    private void copyProperties(List<JurisdictionUiConfigDefinition> listOfConfigs) {
        this.configs = listOfConfigs;
    }
}
