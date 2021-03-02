package uk.gov.hmcts

import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import uk.gov.hmcts.ccd.v2.internal.controller.UIDefinitionController

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class UIDefinitionControllerSpec extends BaseSpringBootSpec {

    @Autowired
    private CaseFactory factory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UIDefinitionController controller;

    @MockBean
    private DefaultDSLContext jooq;

    def "responds to work basket inputs"() {
        when:
        def f = mockMvc.perform(get('/data/internal/case-types/NFD/work-basket-inputs').with(oidcLogin()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
        then:
        f.length() > 0
    }

    def "returns work basket inputs"() {
        when:
        def inputs = controller.getWorkbasketInputsDetails("NFD").getBody().workbasketInputs
        then:
        inputs.size() == 1
        inputs.get(0).label == "Case ID"
        inputs.get(0).getField().id == "caseId"
        inputs.get(0).getField().type.id == "Text"
        inputs.get(0).getField().type.type == "Text"
    }

    def "returns jurisdiction information"() {
        given:
        def jurisdictions = controller.getJurisdictions("read").getBody().jurisdictions
        expect:
        jurisdictions.size() == 1
        jurisdictions[0].caseTypeDefinitions.size() == 1
    }

    def "returns workbasket inputs"() {
        given:
        def inputs = controller.getWorkbasketInputsDetails("NFD").getBody().workbasketInputs
        expect:
        inputs.size() == 1
        inputs[0].label != null
        inputs[0].field != null
    }
}
