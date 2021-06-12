package uk.gov.hmcts.ccd.v2.internal.controller;

import com.google.common.collect.Lists;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import lombok.SneakyThrows;
import org.jooq.generated.enums.CaseState;
import org.jooq.generated.enums.ClaimEvent;
import org.jooq.generated.enums.Event;
import org.jooq.generated.tables.pojos.CaseHistory;
import org.jooq.generated.tables.pojos.ClaimHistory;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseView;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewActionableEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewJurisdiction;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewType;
import uk.gov.hmcts.ccd.domain.model.aggregated.ProfileCaseState;
import uk.gov.hmcts.ccd.v2.V2;
import uk.gov.hmcts.ccd.v2.internal.resource.CaseHistoryViewResource;
import uk.gov.hmcts.ccd.v2.internal.resource.CaseViewResource;
import uk.gov.hmcts.ccf.CaseViewBuilder;
import uk.gov.hmcts.ccf.StateMachine;
import uk.gov.hmcts.ccf.TabBuilder;
import uk.gov.hmcts.unspec.statemachine.ClaimMachine;
import uk.gov.hmcts.unspec.statemachine.CaseMachine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.jooq.generated.Tables.CASES_WITH_STATES;
import static org.jooq.generated.Tables.CASE_HISTORY;
import static org.jooq.generated.Tables.PARTIES;
import static org.jooq.generated.Tables.PARTIES_WITH_CLAIMS;

@RestController
@RequestMapping(path = "/data/internal/cases")
public class UICaseController {
    @Autowired
    private DefaultDSLContext jooq;

    @Autowired
    private ClaimMachine claimController;

    @GetMapping(
        path = "/{caseId}",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.UI_CASE_VIEW
        }
    )
    public ResponseEntity<CaseViewResource> getCaseView(@PathVariable("caseId") String caseId) {

        List<CaseViewEvent> history = getCaseViewHistory(caseId);

        CaseViewBuilder builder = new CaseViewBuilder()
            .newTab("History", "History")
            .field("History", history, null, "CaseHistoryViewer")
            .build();

        builder = buildParties(caseId, builder);
        builder = buildClaims(caseId, builder);

        CaseView view = buildCaseView(caseId);
        view.setTabs(builder.build());

        return ResponseEntity.ok(new CaseViewResource(view));
    }

    @SneakyThrows
    private CaseViewBuilder buildParties(String caseId, CaseViewBuilder builder) {
        List<CaseMachine.CaseParty> parties =
            jooq.select(PARTIES.PARTY_ID, PARTIES.DATA, PARTIES_WITH_CLAIMS.CLAIMS)
                .from(PARTIES)
                .join(PARTIES_WITH_CLAIMS).using(PARTIES.PARTY_ID)
                .where(PARTIES.CASE_ID.eq(Long.valueOf(caseId)))
                .orderBy(PARTIES.CASE_ID.asc())
                .fetchInto(CaseMachine.CaseParty.class);

        builder.newTab("Parties", "Parties")
            .label(renderTemplate(
                "template/parties.md",
                Map.of("parties", parties,
                    "caseId", caseId)));

        return builder;
    }

    @SneakyThrows
    private String renderTemplate(String template,
                                  Map<String, Object> context) {
        PebbleEngine engine = new PebbleEngine.Builder().build();
        PebbleTemplate compiledTemplate = engine.getTemplate(template);

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        return writer.toString();
    }


    @SneakyThrows
    private CaseViewBuilder buildClaims(String caseId, CaseViewBuilder builder) {
        List<ClaimMachine.Claim> claims = claimController.getClaims(caseId);
        for (ClaimMachine.Claim claim : claims) {
            String tabName = getClaimName(claim.getParties());
            TabBuilder tab = builder.newTab(tabName, tabName);

            tab.label(renderTemplate("template/claims.md",
                Map.of("claim", claim,
                    "caseId", caseId)));

            List<ClaimHistory> history =
                claimController.getClaimEvents(String.valueOf(claim.getClaimId()));

            List<Map<String, Object>> hist = Lists.newArrayList();
            for (ClaimHistory c : history) {
                hist.add(Map.of(
                    "name", getHistoryLabel(c.getId().toString()),
                    "forname", c.getUserForename(),
                    "surname", c.getUserSurname(),
                    "time", c.getTimestamp()
                ));
            }

            tab.label(renderTemplate("template/claim_history.html", Map.of("events", hist)));
        }

        return builder;
    }

    private String getClaimName(ClaimMachine.ClaimParties parties) {
        return parties.getClaimants().get(0).name() + " vs.";
    }

    private List<CaseViewEvent> getCaseViewHistory(String caseId) {
        List<CaseHistory> hist = jooq.select()
            .from(CASE_HISTORY)
            .where(CASE_HISTORY.CASE_ID.eq(Long.valueOf(caseId)))
            .orderBy(CASE_HISTORY.TIMESTAMP.desc())
            .fetchInto(CaseHistory.class);
        return hist.stream().map(x -> CaseViewEvent.builder()
            .id(Long.valueOf(x.hashCode()))
            .timestamp(x.getTimestamp())
            .summary(getHistoryLabel(x.getId()))
            .eventId(x.getId())
            .eventName(getHistoryLabel(x.getId()))
            .userId(x.getUserId())
            .userFirstName(x.getUserForename())
            .userLastName(x.getUserSurname())
            .stateId("Open")
            .stateName("Open")
            .build()).collect(toList());
    }

    private String getClaimEventLabel(ClaimEvent c) {
        switch (c) {
            case ClaimIssued:
                return "Issue claim";
            case ConfirmService:
                return "Confirm service";
        }
        throw new RuntimeException();
    }

    private String getActionLabel(String id) {
        if (id.equals("CreateClaim")) {
            return "Create a claim";
        }
        if (id.equals("AddParty")) {
            return "Add a party";
        }
        if (id.equals("AddClaim")) {
            return "Add a claim";
        }
        if (id.equals("CloseCase")) {
            return "Close the case";
        }
        if (id.equals("SubmitAppeal")) {
            return "Submit an appeal";
        }
        return id;
    }

    private String getHistoryLabel(String id) {
        if (id.equals("CreateClaim")) {
            return "Claim Created";
        }
        if (id.equals("ClaimIssued")) {
            return "Claim Issued";
        }
        if (id.equals("AddClaim")) {
            return "Claim Created";
        }
        if (id.equals("AddParty")) {
            return "Party Added";
        }
        if (id.equals("CloseCase")) {
            return "Case closed";
        }
        if (id.equals("SubmitAppeal")) {
            return "Case reopened";
        }
        return id;
    }

    private CaseView buildCaseView(String caseId) {
        CaseView caseView = new CaseView();
        caseView.setCaseId(caseId);
        caseView.setChannels(getChannels());

        caseView.setActionableEvents(getActionableEvents(caseId));

        caseView.setState(getState(caseId));
        CaseViewJurisdiction jurisdiction = new CaseViewJurisdiction();
        jurisdiction.setId("NFD");
        jurisdiction.setName("Civil");
        jurisdiction.setDescription("Civil");
        CaseViewType caseType = new CaseViewType();
        caseType.setJurisdiction(jurisdiction);
        caseType.setDescription("Unspecified claims");
        caseType.setId(caseId);
        caseView.setCaseType(caseType);
        caseView.setEvents(getCaseViewEvents());
        return caseView;
    }

    @Autowired
    StateMachine<CaseState, Event, EventsRecord> statemachine;

    private List<CaseViewActionableEvent> getActionableEvents(String caseId) {
        CaseState currentState = jooq.select(CASES_WITH_STATES.STATE)
            .from(CASES_WITH_STATES)
            .where(CASES_WITH_STATES.CASE_ID.eq(Long.valueOf(caseId)))
            .fetchOne().value1();

        List<CaseViewActionableEvent> result = Lists.newArrayList();
        int t = 1;
        for (Event e : statemachine.getAvailableActions(currentState)) {
            result.add(CaseViewActionableEvent.builder()
                .id("cases_" + e.getLiteral())
                .name(getActionLabel(e.getLiteral()))
                .description(e.getLiteral())
                .order(t++)
                .build());
        }
        return result;
    }

    private ProfileCaseState getState(String caseId) {
        CaseState c = jooq.select(CASES_WITH_STATES.STATE)
            .from(CASES_WITH_STATES)
            .where(CASES_WITH_STATES.CASE_ID.eq(Long.valueOf(caseId)))
            .fetchOne().value1();
        String currentState = c.getLiteral();
        return new ProfileCaseState(currentState, currentState, currentState, currentState);
    }

    private CaseViewEvent createCaseViewEvent(String s) {
        CaseViewEvent event = new CaseViewEvent();
        event.setId(new Random().nextLong());
        event.setComment("Hardcoded TODO");
        event.setEventName(s);
        event.setStateId("Hardcoded TODO");
        event.setStateName("Hardcoded TODO");
        event.setSummary(s);
        event.setTimestamp(LocalDateTime.now());
        event.setUserFirstName("First");
        event.setUserLastName("Last");
        event.setEventId("Hardcoded TODO");
        event.setUserId(String.valueOf(new Random().nextLong()));
        return event;
    }

    private CaseViewEvent[] getCaseViewEvents() {
        Set<String> events = Set.of("AddNotes");
        List<CaseViewEvent> collect = events
            .stream()
            .map(this::createCaseViewEvent)
            .collect(toList());

        CaseViewEvent[] caseViewEvents = new CaseViewEvent[collect.size()];
        return collect.toArray(caseViewEvents);
    }

    private String[] getChannels() {
        String[] strings = new String[1];
        strings[0] = "channel1";
        return strings;
    }

    @GetMapping(
        path = "/{caseId}/events/{eventId}",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.UI_EVENT_VIEW
        }
    )
    public ResponseEntity<CaseHistoryViewResource> getCaseHistoryView(@PathVariable("caseId") String caseId,
                                                                             @PathVariable("eventId") String eventId) {
        throw new RuntimeException();
    }
}
