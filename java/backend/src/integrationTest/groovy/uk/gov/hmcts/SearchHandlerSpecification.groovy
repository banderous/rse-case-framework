package uk.gov.hmcts

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.hmcts.ccf.XUIQuery
import uk.gov.hmcts.unspec.xui.SearchHandler

class SearchHandlerSpecification extends BaseSpringBootSpec {
    @Autowired
    SearchHandler handler;

    @Autowired
    CaseFactory factory;

    def "returns all cases with no params"() {
        def u = factory.createUser("1")
        factory.CreateCase(u)
        factory.CreateCase(u)
        when:
        def query = new XUIQuery(Maps.newHashMap(), 0, 25)
        def results = handler.search(query).results

        then:
        results.size() == 2
    }

    def "search by case ID"() {
        def u = factory.createUser("1")
        factory.CreateCase(u)
        def c2 = factory.CreateCase(u).body
        when:
        def params = ImmutableMap.of("caseId", "-" + String.valueOf(c2.id))
        def query = new XUIQuery(params, 0, 25)
        def results = handler.search(query).results

        then:
        results.size() == 1
        results[0].caseId == c2.id
    }
}
