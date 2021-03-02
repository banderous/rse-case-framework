package uk.gov.hmcts

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import uk.gov.hmcts.ccd.v2.internal.controller.UIStartTriggerController

@Transactional
@SpringBootTest
class UIStartTriggerControllerSpec extends BaseSpringBootSpec {

    @Autowired
    private CaseFactory factory

    @Autowired
    private UIStartTriggerController controller

    def "has dynamic claim events"() {
        given:
        def c = factory.CreateCase(factory.createUser("1"))
        def u = controller.getCaseUpdateViewEvent(c.getBody().getId().toString(),
                "cases_AddClaim", false).getBody()

        expect:
        u.getCaseUpdateViewEvent().caseFields.size() == 4
        u.getCaseUpdateViewEvent().caseFields[2].getFieldTypeDefinition().getFixedListItemDefinitions().size() == 2
    }
}
