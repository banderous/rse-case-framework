package uk.gov.hmcts.ccd.v2.external.resource;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.config.JacksonUtils;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CaseDataResource extends RepresentationModel<RepresentationModel<?>> {

    private JsonNode data;

    public CaseDataResource(@NonNull Object caseData, String caseTypeId, String pageId) {
        this.data = JacksonUtils.convertValueJsonNode(caseData);
    }
}
