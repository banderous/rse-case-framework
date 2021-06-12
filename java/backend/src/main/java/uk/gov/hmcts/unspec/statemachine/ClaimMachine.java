package uk.gov.hmcts.unspec.statemachine;

import static org.jooq.generated.Tables.CLAIMS;
import static org.jooq.generated.Tables.CLAIMS_WITH_PARTIES;
import static org.jooq.generated.Tables.CLAIMS_WITH_STATES;
import static org.jooq.generated.Tables.CLAIM_EVENTS;
import static org.jooq.generated.Tables.CLAIM_HISTORY;


import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jooq.generated.enums.ClaimEvent;
import org.jooq.generated.enums.ClaimState;
import org.jooq.generated.tables.pojos.ClaimHistory;
import org.jooq.generated.tables.records.ClaimEventsRecord;
import org.jooq.generated.tables.records.ClaimsRecord;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.ccf.StateMachine;
import uk.gov.hmcts.unspec.dto.ConfirmService;
import uk.gov.hmcts.unspec.dto.Party;
import uk.gov.hmcts.unspec.event.CreateClaim;

@Configuration
public class ClaimMachine {

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

    @Bean
    public StateMachine<ClaimState, ClaimEvent, ClaimEventsRecord> build() {
        StateMachine<ClaimState, ClaimEvent, ClaimEventsRecord> result = new StateMachine<>(
            "claims", ClaimEvent.class, jooq,
            this::create, ClaimEvent.ClaimIssued, CLAIM_EVENTS, CLAIM_EVENTS.CLAIM_ID, CLAIM_EVENTS.STATE, CLAIM_EVENTS.ID, CLAIM_EVENTS.USER_ID,
            CLAIM_EVENTS.SEQUENCE_NUMBER);
        result.initialState(ClaimState.Issued, this::onCreate)
            .addTransition(ClaimState.Issued,
                ClaimState.ServiceConfirmed, ClaimEvent.ConfirmService, this::confirmService)
            .field(ConfirmService::getName)
            .field(ConfirmService::getRole);
        return result;
    }

    private Long create() {
        ClaimsRecord r = jooq.newRecord(CLAIMS);
        r.store();
        return r.getClaimId();
    }

    public List<Claim> getClaims(@PathVariable("caseId") String caseId) {
        List<Claim> result = jooq.select()
            .from(CLAIMS_WITH_STATES)
            .join(CLAIMS_WITH_PARTIES).using(CLAIMS_WITH_STATES.CLAIM_ID)
            .where(CLAIMS_WITH_STATES.CASE_ID.eq(Long.valueOf(caseId)))
            .orderBy(CLAIMS_WITH_STATES.CLAIM_ID.asc())
            .fetchInto(Claim.class);

        for (Claim claim : result) {
            StateMachine<ClaimState, ClaimEvent, ClaimEventsRecord> statemachine = build();
            statemachine.rehydrate(claim.claimId);
            claim.setAvailableEvents(statemachine.getAvailableActions(claim.state));
        }

        return result;
    }

    public List<ClaimHistory> getClaimEvents(@PathVariable("claimId") String claimId) {
        return jooq.select()
            .from(CLAIM_HISTORY)
            .where(CLAIM_HISTORY.CLAIM_ID.eq(Long.valueOf(claimId)))
            .orderBy(CLAIM_HISTORY.TIMESTAMP.desc())
            .fetch()
            .into(ClaimHistory.class);
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
