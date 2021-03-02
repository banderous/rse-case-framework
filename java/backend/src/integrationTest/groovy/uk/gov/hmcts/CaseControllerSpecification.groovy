package uk.gov.hmcts

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import groovy.json.JsonOutput
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.generated.enums.CaseState
import org.jooq.generated.enums.Event
import org.jooq.impl.DSL
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
import uk.gov.hmcts.ccf.controller.kase.ApiEventCreation
import uk.gov.hmcts.ccf.controller.kase.CaseController
import uk.gov.hmcts.unspec.CaseHandlerImpl
import uk.gov.hmcts.unspec.dto.AddClaim
import uk.gov.hmcts.unspec.event.CloseCase
import uk.gov.hmcts.unspec.event.CreateClaim
import uk.gov.hmcts.unspec.event.SubmitAppeal

import javax.sql.DataSource
import java.nio.charset.StandardCharsets
import java.time.LocalDate

import static org.jooq.generated.Tables.CASES
import static org.jooq.impl.DSL.count
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@Transactional
class CaseControllerSpecification extends BaseSpringBootSpec {

    @Autowired
    private CaseController controller

    @Autowired
    private UICaseController uiCaseController;

    @Autowired
    private DataSource dataSource

    @Autowired
    private CaseFactory factory;

    @Autowired
    private CaseHandlerImpl handler;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    def "exports openAPI specification"() {
        when:
        def f = mockMvc.perform(get('/v3/api-docs').with(oidcLogin()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
        then:
        new File("build/open-api.yaml").write(JsonOutput.prettyPrint(f))
    }

    def "A case can be created"() {
        given:
        def response = factory.CreateCase()

        expect: "Status is 201 and the response is the case ID"
        response.getStatusCode() == HttpStatus.CREATED
        response.getHeaders().getLocation().toString().contains("/cases")
    }

    def "a case can be retrieved when logged in"() {
        given:
        def result = factory.CreateCase().getBody()
        def json = mockMvc.perform(get("/web/cases/" + result.getId()).with(oidcLogin()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
        CaseController.CaseActions a = new ObjectMapper().readValue(json, CaseController.CaseActions.class)

        expect:
        a.getState() == CaseState.Created
        a.getActions().isEmpty() == false
    }

    def "an invalid case is not created"() {
        when:
        def count = caseCount()
        CreateClaim sol = CreateClaim.builder().defendantReference("@!").claimantReference("@").build()
        JsonNode j = new ObjectMapper().valueToTree(sol)
        controller.createCase(new ApiEventCreation('Create', j), factory.createUser())

        then:
        thrown IllegalArgumentException
        caseCount() == count
    }

    def "A new case has a creation event"() {
        given:
        def response = factory.CreateCase().getBody()
        def events = controller.getCaseEvents(String.valueOf(response.getId()))
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
        def s = controller.getParties(String.valueOf(response.getId()));
        def parties = controller.getParties(String.valueOf(response.getId()))

        expect: "Case has two parties"
        parties.size() == 2
        parties[0].partyId > 0
        parties[0].data != null
        parties[0].claims.claimant.size() == 1
    }

    def "A party cannot be on both sides of a claim"() {
        when:
        def userId = factory.createUser()
        def response = factory.CreateCase(userId).getBody()
        def parties = controller.getParties(String.valueOf(response.getId()))
        Long partyId = parties[0].partyId
        handler.addClaim(StateMachine.TransitionContext.builder().userId(userId).entityId(response.getId()).build(),
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
        ApiEventCreation event = new ApiEventCreation(Event.CloseCase, new CloseCase("Case withdrawn"))
        controller.createEvent(id, event, userId)

        expect:
        controller.getCase(String.valueOf(id)).state == CaseState.Closed
    }

    def "A closed case can be reopened"() {
        given:
        def userId = factory.createUser()
        def response = factory.CreateCase(userId)
        def id = response.getBody().id
        ApiEventCreation event = new ApiEventCreation(Event.CloseCase, new CloseCase("Case withdrawn"))
        controller.createEvent(id, event, userId)
        event = new ApiEventCreation(Event.SubmitAppeal, new SubmitAppeal("New evidence"))
        controller.createEvent(id, event, userId)

        expect:
        controller.getCase(String.valueOf(id)).state == CaseState.Stayed
    }

    private int caseCount() {
        // Read in a new transaction.
        DSLContext create = DSL.using(dataSource, SQLDialect.DEFAULT);
        return create.select(count()).from(CASES).fetchSingle().value1();
    }

    def "Adds a new party"() {
        when:
        def c = factory.CreateCase()
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
        controller.getCase(c.getBody().getId().toString())
        uiCaseController.getCaseView(c.getBody().getId().toString())
    }
}
