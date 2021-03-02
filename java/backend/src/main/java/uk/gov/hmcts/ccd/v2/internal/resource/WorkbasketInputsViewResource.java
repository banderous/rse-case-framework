package uk.gov.hmcts.ccd.v2.internal.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.search.Field;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class WorkbasketInputsViewResource extends RepresentationModel {

    @Data
    @Builder
    public static class WorkbasketInputView {
        private String label;
        private int order;
        private Field field;
        @JsonProperty("display_context_parameter")
        private String displayContextParameter;
    }

    @Singular
    private List<WorkbasketInputView> workbasketInputs;

}
