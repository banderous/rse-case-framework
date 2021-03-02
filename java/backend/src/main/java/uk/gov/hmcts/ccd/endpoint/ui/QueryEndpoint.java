package uk.gov.hmcts.ccd.endpoint.ui;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseHistoryView;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseUpdateViewEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseView;
import uk.gov.hmcts.ccd.domain.model.aggregated.JurisdictionDisplayProperties;
import uk.gov.hmcts.ccd.domain.model.definition.CaseStateDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.search.SearchInput;
import uk.gov.hmcts.ccd.domain.model.search.SearchResultView;
import uk.gov.hmcts.ccd.domain.model.search.WorkbasketInput;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/aggregated"
    )
public class QueryEndpoint {

    public QueryEndpoint() {

    }

    @Deprecated
    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types", method = RequestMethod.GET)
    public List<CaseTypeDefinition> getCaseTypes(@PathVariable("jid") final String jurisdictionId,
                                                 @RequestParam(value = "access", required = true) String access) {
        throw new RuntimeException();
    }

    @GetMapping(value = "/caseworkers/{uid}/jurisdictions")
    public List<JurisdictionDisplayProperties> getJurisdictions(@RequestParam(value = "access") String access) {
        return List.of(
            JurisdictionDisplayProperties.builder()
                .id("NFD")
            .name("Civil")
            .description("Civil")
            .caseType(CaseTypeDefinition.builder()
                .id("NFD")
                .name("Unspecified claims")
                .description("Unspecified claims")
                .state(CaseStateDefinition.builder()
                    .id("Open")
                    .name("Open")
                    .description("Open")
                    .titleDisplay("Open")
                    .build())
                .build())
                .build()
        );
    }

    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases",
        method = RequestMethod.GET)
    public SearchResultView searchNew(@PathVariable("jid") final String jurisdictionId,
                                      @PathVariable("ctid") final String caseTypeId,
                                      @RequestParam Map<String, String> params) {
        throw new RuntimeException();
    }

    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/inputs",
        method = RequestMethod.GET)
    public SearchInput[] findSearchInputDetails(@PathVariable("uid") final String uid,
                                                @PathVariable("jid") final String jurisdictionId,
                                                @PathVariable("ctid") final String caseTypeId) {
        throw new RuntimeException();
    }

    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/work-basket-inputs",
        method = RequestMethod.GET)
    public WorkbasketInput[] findWorkbasketInputDetails(@PathVariable("uid") final String uid,
                                                        @PathVariable("jid") final String jurisdictionId,
                                                        @PathVariable("ctid") final String caseTypeId) {
        throw new RuntimeException();
    }

    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}",
        method = RequestMethod.GET)
    public CaseView findCase(@PathVariable("jid") final String jurisdictionId,
                             @PathVariable("ctid") final String caseTypeId,
                             @PathVariable("cid") final String cid) {
        throw new RuntimeException();
    }

    @RequestMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/event-triggers/{etid}",
        method = RequestMethod.GET)
    public CaseUpdateViewEvent getEventTriggerForCaseType(@PathVariable("uid") String userId,
                                                          @PathVariable("jid") String jurisdictionId,
                                                          @PathVariable("ctid") String caseTypeId,
                                                          @PathVariable("etid") String eventTriggerId,
                                                          @RequestParam(value = "ignore-warning",
                                                           required = false) Boolean ignoreWarning) {
        throw new RuntimeException();
    }

    @RequestMapping(
        value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/event-triggers/{etid}",
        method = RequestMethod.GET)
    public CaseUpdateViewEvent getEventTriggerForCase(@PathVariable("uid") String userId,
                                                      @PathVariable("jid") String jurisdictionId,
                                                      @PathVariable("ctid") String caseTypeId,
                                                      @PathVariable("cid") String caseId,
                                                      @PathVariable("etid") String eventId,
                                                      @RequestParam(value = "ignore-warning",
                                                       required = false) Boolean ignoreWarning) {
        throw new RuntimeException();
    }

    @RequestMapping(
        value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/drafts/{did}/event-triggers/{etid}",
        method = RequestMethod.GET)
    public CaseUpdateViewEvent getEventTriggerForDraft(@PathVariable("uid") String userId,
                                                       @PathVariable("jid") String jurisdictionId,
                                                       @PathVariable("ctid") String caseTypeId,
                                                       @PathVariable("did") String draftId,
                                                       @PathVariable("etid") String eventId,
                                                       @RequestParam(value = "ignore-warning",
                                                        required = false) Boolean ignoreWarning) {
        throw new RuntimeException();
    }

    @RequestMapping(
        value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/events/{eventId}/case-history",
        method = RequestMethod.GET)
    public CaseHistoryView getCaseHistoryForEvent(@PathVariable("jid") final String jurisdictionId,
                                                  @PathVariable("ctid") final String caseTypeId,
                                                  @PathVariable("cid") final String caseReference,
                                                  @PathVariable("eventId") final Long eventId) {
        throw new RuntimeException();
    }

}
