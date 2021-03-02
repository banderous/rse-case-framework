package uk.gov.hmcts

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import uk.gov.hmcts.ccd.v2.internal.controller.UICaseController

@SpringBootTest
@Transactional
class UICaseControllerSpec extends BaseSpringBootSpec {
    @Autowired
    UICaseController controller;

    @Autowired
    private CaseFactory factory;

    def "renders the case view"() {
        given:
        def c = factory.CreateCase(factory.createUser("1"))
        def view = controller.getCaseView(String.valueOf(c.getBody().getId())).getBody()

        expect:
        view.getTabs().size() > 0
        view.getTabs()[0].getFields().size() > 0
    }
}
