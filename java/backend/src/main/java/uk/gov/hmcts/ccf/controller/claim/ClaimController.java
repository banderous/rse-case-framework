package uk.gov.hmcts.ccf.controller.claim;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jooq.Record2;
import org.jooq.generated.enums.ClaimEvent;
import org.jooq.generated.enums.ClaimState;
import org.jooq.generated.tables.pojos.ClaimHistory;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccf.StateMachine;
import uk.gov.hmcts.unspec.dto.ConfirmService;
import uk.gov.hmcts.unspec.dto.Party;
import uk.gov.hmcts.unspec.event.CreateClaim;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.jooq.generated.Tables.CLAIMS_WITH_PARTIES;
import static org.jooq.generated.Tables.CLAIMS_WITH_STATES;
import static org.jooq.generated.Tables.CLAIM_EVENTS;
import static org.jooq.generated.Tables.CLAIM_HISTORY;

@RestController
@RequestMapping("/web")
public class ClaimController {

    @Autowired
    DefaultDSLContext jooq;

    @Data
    @NoArgsConstructor
    public static class Claim {
        Long claimId;
        Long caseId;
        Long lowerAmount;
        Long higherAmount;
        ClaimState state;
        ClaimParties parties;
        Set<ClaimEvent> availableEvents;
    }

    @GetMapping(path = "/cases/{caseId}/claims")
    public List<Claim> getClaims(@PathVariable("caseId") String caseId) {
        List<Claim> result = jooq.select()
            .from(CLAIMS_WITH_STATES)
            .join(CLAIMS_WITH_PARTIES).using(CLAIMS_WITH_STATES.CLAIM_ID)
            .where(CLAIMS_WITH_STATES.CASE_ID.eq(Long.valueOf(caseId)))
            .orderBy(CLAIMS_WITH_STATES.CLAIM_ID.asc())
            .fetchInto(Claim.class);

        for (Claim claim : result) {
            StateMachine<ClaimState, ClaimEvent> statemachine = build(claim.state);
            claim.setAvailableEvents(statemachine.getAvailableActions(claim.state));
        }

        return result;
    }

    @GetMapping(path = "/claims/{claimId}/events")
    public List<ClaimHistory> getClaimEvents(@PathVariable("claimId") String claimId) {
        return jooq.select()
            .from(CLAIM_HISTORY)
            .where(CLAIM_HISTORY.CLAIM_ID.eq(Long.valueOf(claimId)))
            .orderBy(CLAIM_HISTORY.TIMESTAMP.desc())
            .fetch()
            .into(ClaimHistory.class);
    }

    public ResponseEntity<String> createEvent(Long claimId,
                                              ClaimEvent event,
                                              JsonNode data,
                                              String userId) {
        Record2<Integer, ClaimState> record = jooq.select(CLAIM_EVENTS.SEQUENCE_NUMBER, CLAIM_EVENTS.STATE)
            .from(CLAIM_EVENTS)
            .where(CLAIM_EVENTS.CLAIM_ID.eq(claimId))
            .orderBy(CLAIM_EVENTS.SEQUENCE_NUMBER.desc())
            .limit(1)
            .fetchSingle();

        StateMachine<ClaimState, ClaimEvent> statemachine = build(record.component2());
        StateMachine.TransitionContext context = new StateMachine.TransitionContext(userId, claimId);
        statemachine.handleEvent(context, event, data);

        jooq.insertInto(CLAIM_EVENTS)
            .columns(CLAIM_EVENTS.ID, CLAIM_EVENTS.CLAIM_ID, CLAIM_EVENTS.STATE,
                CLAIM_EVENTS.USER_ID)
            .values(event, claimId, statemachine.getState(), userId)
            .execute();

        return ResponseEntity.created(URI.create("/claims/" + claimId))
            .body("");
    }

    public StateMachine<ClaimState, ClaimEvent> build(ClaimState state) {
        StateMachine<ClaimState, ClaimEvent> result = new StateMachine<>();
        result.initialState(ClaimState.Issued, this::onCreate)
            .addTransition(ClaimState.Issued,
                ClaimState.ServiceConfirmed, ClaimEvent.ConfirmService, this::confirmService)
            .field(ConfirmService::getName)
            .field(ConfirmService::getRole);
        result.rehydrate(state);
        return result;
    }

    public void onCreate(StateMachine.TransitionContext transitionContext, CreateClaim c) {

    }

    public void confirmService(StateMachine.TransitionContext context, ConfirmService service) {
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClaimParties {
        List<Party> claimants;
        List<Party> defendants;
    }
}
