package uk.gov.hmcts

import com.google.common.io.Resources
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import uk.gov.hmcts.ccd.v2.internal.controller.UICaseSearchController

import java.nio.charset.StandardCharsets

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Transactional
@AutoConfigureMockMvc
class UICaseSearchControllerSpec extends BaseSpringBootSpec {

    @Autowired
    private UICaseSearchController controller;

    @Autowired
    private CaseFactory factory;

    @Autowired
    private MockMvc mockMvc;

    def "searches cases"() {
        given:
        URL url = Resources.getResource("requests/data/internal/searchCases.json");
        String json = Resources.toString(url, StandardCharsets.UTF_8);
        def c = factory.CreateCase()
        def result = controller.searchCases("", "", json).getBody()
        def fields = result.getHeaders().get(0).getFields()

        expect:
        result.cases.size() > 0
        result.total == 1
        result.cases.get(0).fields.get("caseId") == c.getBody().id.toString()
        result.cases.get(0).fields.get("rowCount") == "1"

        fields.size() == 3
        fields.get(0).getCaseFieldTypeDefinition().type == "Number"
        fields.get(0).label == "Case ID"
        fields.get(1).getCaseFieldTypeDefinition().type == "Text"
        fields.get(2).getCaseFieldTypeDefinition().type == "Number"
    }

    def "has jurisdictions"() {
        when:
        def f = mockMvc.perform(get('/aggregated/caseworkers/:uid/jurisdictions?access=read').with(oidcLogin()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
        then:
        f.length() > 0
    }

}
