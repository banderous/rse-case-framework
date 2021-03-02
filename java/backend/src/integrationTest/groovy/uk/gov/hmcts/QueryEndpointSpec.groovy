package uk.gov.hmcts


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.hmcts.ccd.endpoint.ui.QueryEndpoint

@SpringBootTest
class QueryEndpointSpec extends BaseSpringBootSpec {

    @Autowired
    private QueryEndpoint query;

    def "returns jurisdiction information"() {
        given:
        def jurisdictions = query.getJurisdictions("read")
        expect:
        jurisdictions.size() == 1
        jurisdictions[0].caseTypes.size() == 1
        jurisdictions[0].caseTypes[0].states.size() >= 1
    }
}
