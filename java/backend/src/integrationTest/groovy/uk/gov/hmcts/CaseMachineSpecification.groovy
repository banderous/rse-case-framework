package uk.gov.hmcts

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import groovy.json.JsonOutput
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.generated.enums.CaseState
import org.jooq.generated.enums.Event
import org.jooq.generated.tables.pojos.CaseHistory
import org.jooq.generated.tables.records.EventsRecord
import org.jooq.impl.DSL
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext
import uk.gov.hmcts.ccd.v2.internal.controller.UICaseController
import uk.gov.hmcts.ccf.StateMachine
import uk.gov.hmcts.unspec.dto.Address
import uk.gov.hmcts.unspec.statemachine.CaseMachine
import uk.gov.hmcts.unspec.dto.AddClaim
import uk.gov.hmcts.unspec.event.CloseCase
import uk.gov.hmcts.unspec.event.CreateClaim
import uk.gov.hmcts.unspec.event.SubmitAppeal

import javax.sql.DataSource
import java.nio.charset.StandardCharsets
import java.time.LocalDate

import static org.jooq.generated.Tables.CASES
import static org.jooq.generated.Tables.CASE_HISTORY
import static org.jooq.generated.Tables.CASE_HISTORY
import static org.jooq.generated.Tables.CASE_HISTORY
import static org.jooq.impl.DSL.count
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@Transactional
class CaseMachineSpecification extends BaseSpringBootSpec {

    @Autowired
    private CaseMachine controller

    @Autowired
    private StateMachine<CaseState, Event, EventsRecord> stateMachine;

    @Autowired
    private UICaseController uiCaseController;

    @Autowired
    private DataSource dataSource

    @Autowired
    private CaseFactory factory;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DefaultDSLContext jooq;

    private MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    def "A case can be created"() {
        given:
        def response = factory.CreateCase()

        expect: "Status is 201 and the response is the case ID"
        response.getStatusCode() == HttpStatus.CREATED
        response.getHeaders().getLocation().toString().contains("/cases")
    }

    def "an invalid case is not created"() {
        when:
        def userId = factory.createUser()
        def count = caseCount()
        CreateClaim sol = CreateClaim.builder().defendantReference("@!").claimantReference("@").build()
        JsonNode j = new ObjectMapper().valueToTree(sol)
        stateMachine.onCreated(userId, j)

        then:
        thrown IllegalArgumentException
        caseCount() == count
    }

    def "A new case has a creation event"() {
        given:
        def response = factory.CreateCase().getBody()
        def events = jooq.select()
                .from(CASE_HISTORY)
                .where(CASE_HISTORY.CASE_ID.eq(response.getId()))
                .orderBy(CASE_HISTORY.TIMESTAMP.desc())
                .fetchInto(CaseHistory.class);
        def event = events.get(0)


        expect: "Case has events for case and claim creation"
        events.size() == 2
        LocalDate.now() == event.getTimestamp().toLocalDate()
        event.userForename == "John"
        event.userSurname == "Smith"
    }

    def "A new case has two parties"() {
        given:
        def response = factory.CreateCase().getBody()
        def parties = controller.getParties(String.valueOf(response.getId()))

        expect: "Case has two parties"
        parties.size() == 2
        parties[0].partyId > 0
        parties[0].data != null
        parties[0].claims.claimant.size() == 1
    }

    def "A parties address can be changed"() {
        given:
        def userId = factory.createUser()
        def response = factory.CreateCase(userId).getBody()
        def parties = controller.getParties(String.valueOf(response.getId()))
        def address = Address.builder().address1("new line1")
                .address2("new line2").build();
        def data = new ObjectMapper().valueToTree(address);
        def pid = parties.get(0).getPartyId()
        def context = StateMachine.TransitionContext.builder()
                .userId(userId)
                .entityId(response.getId())
                .param(pid.toString())
                .build()
        stateMachine.handleEvent(context, Event.ChangePartyAddress, data)
        parties = controller.getParties(String.valueOf(response.getId()))
        def party = parties.find {x -> (x.getPartyId() == pid) }

        expect: "Case has two parties"
        party.data.address.address1 == "new line1"
        party.data.address.address2 == "new line2"
    }

    def "A party cannot be on both sides of a claim"() {
        when:
        def userId = factory.createUser()
        def response = factory.CreateCase(userId).getBody()
        def parties = controller.getParties(String.valueOf(response.getId()))
        Long partyId = parties[0].partyId
        stateMachine.handleEvent(userId, response.getId(), Event.AddClaim,
                AddClaim.builder()
                        .lowerValue(10)
                        .higherValue(20)
                        .defendants(Set.of(partyId))
                        .claimants(Set.of(partyId)).build())

        then:
        thrown DuplicateKeyException
    }


    def "An event can change a case's state"() {
        given:
        def userId = factory.createUser()
        def response = factory.CreateCase(userId)
        def id = response.getBody().id
        def data = new ObjectMapper().valueToTree(new CloseCase("Withdrawn"));
        stateMachine.handleEvent(userId, id, Event.CloseCase, data)

        expect:
        stateMachine.state == CaseState.Closed
    }

    def "A closed case can be reopened"() {
        given:
        def userId = factory.createUser()
        def response = factory.CreateCase(userId)
        def id = response.getBody().id
        def data = new ObjectMapper().valueToTree(new CloseCase("Withdrawn"));
        stateMachine.handleEvent(userId, id, Event.CloseCase, data)
        data = new ObjectMapper().valueToTree(new SubmitAppeal("New evidence"));
        stateMachine.handleEvent(userId, id, Event.SubmitAppeal, data)

        expect:
        stateMachine.state == CaseState.Stayed
    }

    private int caseCount() {
        // Read in a new transaction.
        DSLContext create = DSL.using(dataSource, SQLDialect.DEFAULT);
        return create.select(count()).from(CASES).fetchSingle().value1();
    }

    def "Adds a new party"() {
        when:
        def c = factory.CreateCase()
        stateMachine.rehydrate(c.getBody().getId());
        URL url = Resources.getResource("requests/data/cases/157/addParty.json");
        String body = Resources.toString(url, StandardCharsets.UTF_8);
        String path = String.format('/data/cases/%s/events', c.getBody().getId());
        mockMvc.perform(post(path)
                .with(csrf())
                .with(oidcLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
        then:
        uiCaseController.getCaseView(c.getBody().getId().toString())
    }
}
