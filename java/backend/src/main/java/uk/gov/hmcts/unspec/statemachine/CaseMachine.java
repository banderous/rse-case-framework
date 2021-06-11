package uk.gov.hmcts.unspec.statemachine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jooq.JSONB;
import org.jooq.generated.enums.CaseState;
import org.jooq.generated.enums.ClaimEvent;
import org.jooq.generated.enums.ClaimState;
import org.jooq.generated.enums.Event;
import org.jooq.generated.enums.PartyRole;
import org.jooq.generated.tables.pojos.CaseHistory;
import org.jooq.generated.tables.records.CasesRecord;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccf.EventBuilder;
import uk.gov.hmcts.ccf.StateMachine;
import uk.gov.hmcts.unspec.dto.AddClaim;
import uk.gov.hmcts.unspec.dto.AddParty;
import uk.gov.hmcts.unspec.dto.Individual;
import uk.gov.hmcts.unspec.dto.Party;

import java.util.List;
import java.util.Set;
import uk.gov.hmcts.unspec.event.CloseCase;
import uk.gov.hmcts.unspec.event.CreateClaim;
import uk.gov.hmcts.unspec.event.ReopenCase;


import static org.jooq.generated.Tables.CASES;
import static org.jooq.generated.Tables.CASE_HISTORY;
import static org.jooq.generated.Tables.CLAIMS;
import static org.jooq.generated.Tables.CLAIM_EVENTS;
import static org.jooq.generated.Tables.CLAIM_PARTIES;
import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.PARTIES;
import static org.jooq.generated.Tables.PARTIES_WITH_CLAIMS;

@RestController
@RequestMapping("/web/cases")
public class CaseMachine {

    @Autowired
    DefaultDSLContext jooq;

    @Bean
    public StateMachine<CaseState, Event, EventsRecord> buildCase() {
        StateMachine<CaseState, Event, EventsRecord> result = new StateMachine<>(
            "cases", Event.class, jooq,
            this::create, Event.CreateClaim, EVENTS, EVENTS.CASE_ID, EVENTS.STATE, EVENTS.ID, EVENTS.USER_ID, EVENTS.SEQUENCE_NUMBER);
        result.initialState(CaseState.Created, this::onCreate);

        result.addEvent(CaseState.Created, Event.AddParty, this::addParty)
            .field(AddParty::getPartyType)
            .showGroup("partyType!=\"Individual\"")
            .field(AddParty::getName)
            .showGroup("partyType=\"Individual\"")
            .field(AddParty::getTitle)
            .field(AddParty::getFirstName)
            .field(AddParty::getLastName)
            .field(AddParty::getDateOfBirth);

        result.dynamicEvent(CaseState.Created, Event.AddClaim, this::addClaim, this::buildAddClaimEvent);

        result.addTransition(CaseState.Created, CaseState.Closed, Event.CloseCase, this::closeCase)
            .field(CloseCase::getReason);

        result.addTransition(CaseState.Closed, CaseState.Stayed, Event.SubmitAppeal, this::reopenCase)
            .field(ReopenCase::getReason);
        return result;
    }

    private Long create() {
        CasesRecord r = jooq.newRecord(CASES);
        r.store();
        return r.getCaseId();
    }

    private void buildAddClaimEvent(Long caseId, EventBuilder<AddClaim> builder) {
        List<CaseMachine.CaseParty> parties = getParties(String.valueOf(caseId));
        Map<Long, String> options = Maps.newHashMap();
        for (CaseMachine.CaseParty party : parties) {
            options.put(party.getPartyId(), party.getData().name());
        }

        builder.field(AddClaim::getLowerValue)
            .field(AddClaim::getHigherValue)
            .nextPage()
            .multiSelect(AddClaim::getClaimants, options)
            .multiSelect(AddClaim::getDefendants, options, 2);
    }

    @SneakyThrows
    private void addParty(StateMachine.TransitionContext context, AddParty party) {
        jooq.insertInto(PARTIES, PARTIES.CASE_ID, PARTIES.DATA)
            .values(context.getEntityId(), JSONB.valueOf(getObjectMapper().writeValueAsString(party)))
            .execute();
    }

    @SneakyThrows
    public void addClaim(StateMachine.TransitionContext context, AddClaim claim) {
        Set<Long> claimantIds = claim.getClaimants();
        if (claimantIds.size() == 0) {
            throw new IllegalArgumentException("Must have at least one defendant!");
        }

        Set<Long> defendantIds = claim.getDefendants();
        if (defendantIds.size() == 0) {
            throw new IllegalArgumentException("Must have at least one claimant!");
        }

        Long claimId = jooq.insertInto(CLAIMS, CLAIMS.CASE_ID, CLAIMS.LOWER_AMOUNT, CLAIMS.HIGHER_AMOUNT)
            .values(context.getEntityId(), claim.getLowerValue(), claim.getHigherValue())
            .returning(CLAIMS.CLAIM_ID)
            .fetchOne().getClaimId();

        jooq.insertInto(CLAIM_EVENTS, CLAIM_EVENTS.CLAIM_ID, CLAIM_EVENTS.ID, CLAIM_EVENTS.STATE, CLAIM_EVENTS.USER_ID)
            .values(claimId, ClaimEvent.ClaimIssued, ClaimState.Issued, context.getUserId())
            .execute();

        List<Object[]> claimParties = claimantIds.stream().map(x -> {
            return new Object[]{claimId, x, PartyRole.claimant};
        }).collect(Collectors.toList());

        claimParties.addAll(defendantIds.stream().map(x -> {
            return new Object[]{claimId, x, PartyRole.defendant};
        }).collect(Collectors.toList()));


        jooq.loadInto(CLAIM_PARTIES)
            .loadArrays(claimParties)
            .fields(CLAIM_PARTIES.CLAIM_ID, CLAIM_PARTIES.PARTY_ID, CLAIM_PARTIES.PARTY_TYPE)
            .execute();
    }

    @SneakyThrows
    public void onCreate(StateMachine.TransitionContext context, CreateClaim request) {
        String ref = request.getClaimantReference();
        if (ref == null || ref.length() == 0 || ref.contains("@")) {
            throw new IllegalArgumentException("Invalid reference!");
        }

        if (request.getClaimant() instanceof Individual) {
            Individual i = (Individual) request.getClaimant();
            if (i.getTitle() == null || i.getTitle().length() == 0) {
                throw new RuntimeException();
            }
        }

        List<Long> partyIds = jooq.insertInto(PARTIES, PARTIES.CASE_ID, PARTIES.DATA)
            .values(context.getEntityId(), JSONB.valueOf(
                new ObjectMapper().writeValueAsString(request.getClaimant())))
            .values(context.getEntityId(), JSONB.valueOf(
                new ObjectMapper().writeValueAsString(request.getDefendant())))
            .returningResult(PARTIES.PARTY_ID)
            .fetch()
            .getValues(PARTIES.PARTY_ID);

        addClaim(context, AddClaim.builder()
            .lowerValue(request.getLowerValue())
            .higherValue(request.getHigherValue())
            .claimants(Set.of(partyIds.get(0)))
            .defendants(Set.of(partyIds.get(1)))
            .build());

    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());
        return mapper;
    }

    private void reopenCase(StateMachine.TransitionContext context, ReopenCase r) {

    }

    private void closeCase(StateMachine.TransitionContext context, CloseCase t) {

    }

    @GetMapping(path = "/{caseId}/events")
    public List<CaseHistory> getCaseEvents(@PathVariable("caseId") String caseId) {
        return jooq.select()
            .from(CASE_HISTORY)
            .where(CASE_HISTORY.CASE_ID.eq(Long.valueOf(caseId)))
            .orderBy(CASE_HISTORY.TIMESTAMP.desc())
            .fetchInto(CaseHistory.class);
    }

    @Data
    @AllArgsConstructor
    public static class CaseParty {
        Long partyId;
        Party data;
        PartyClaims claims;

        @NoArgsConstructor
        @Data
        public static class PartyClaims {
            List<Long> claimant;
            List<Long> defendant;
        }
    }

    @GetMapping(path = "/{caseId}/parties")
    public List<CaseParty> getParties(@PathVariable("caseId") String caseId) {
        return jooq.select(PARTIES.PARTY_ID, PARTIES.DATA, PARTIES_WITH_CLAIMS.CLAIMS)
                .from(PARTIES)
                .join(PARTIES_WITH_CLAIMS).using(PARTIES.PARTY_ID)
                .where(PARTIES.CASE_ID.eq(Long.valueOf(caseId)))
                .orderBy(PARTIES.CASE_ID.asc())
                .fetchInto(CaseParty.class);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class CaseActions {
        private long id;
        private CaseState state;
        private Set<Event> actions;
    }
}
