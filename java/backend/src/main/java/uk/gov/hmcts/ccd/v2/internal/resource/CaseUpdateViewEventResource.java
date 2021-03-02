package uk.gov.hmcts.ccd.v2.internal.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseUpdateViewEvent;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CaseUpdateViewEventResource extends RepresentationModel {
    private static final Logger LOG = LoggerFactory.getLogger(CaseUpdateViewEventResource.class);

    private enum Origin { DRAFT, CASE, CASE_TYPE }

    @JsonUnwrapped
    private CaseUpdateViewEvent caseUpdateViewEvent;

    public static CaseUpdateViewEventResource forCase(@NonNull CaseUpdateViewEvent caseUpdateViewEvent, String caseId,
                                                      Boolean ignoreWarning) {
        return new CaseUpdateViewEventResource(caseUpdateViewEvent, caseId, ignoreWarning, Origin.CASE);
    }

    public static CaseUpdateViewEventResource forCaseType(@NonNull CaseUpdateViewEvent caseUpdateViewEvent,
                                                          String caseType, Boolean ignoreWarning) {
        return new CaseUpdateViewEventResource(caseUpdateViewEvent, caseType, ignoreWarning, Origin.CASE_TYPE);
    }

    public static CaseUpdateViewEventResource forDraft(@NonNull CaseUpdateViewEvent caseUpdateViewEvent, String draftId,
                                                       Boolean ignoreWarning) {
        return new CaseUpdateViewEventResource(caseUpdateViewEvent, draftId, ignoreWarning, Origin.DRAFT);
    }

    @JsonIgnore
    public CaseUpdateViewEvent getCaseUpdateViewEvent() {
        return caseUpdateViewEvent;
    }

    private CaseUpdateViewEventResource(@NonNull CaseUpdateViewEvent caseUpdateViewEvent, String id,
                                        Boolean ignoreWarning, Origin origin) {
        copyProperties(caseUpdateViewEvent);
    }

    private void copyProperties(CaseUpdateViewEvent caseUpdateViewEvent) {
        this.caseUpdateViewEvent = caseUpdateViewEvent;
    }
}
