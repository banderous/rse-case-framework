package uk.gov.hmcts

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.generated.enums.ClaimEvent
import org.jooq.generated.enums.ClaimState
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import uk.gov.hmcts.ccf.controller.claim.ClaimController
import uk.gov.hmcts.unspec.dto.ConfirmService

@SpringBootTest
@Transactional
class ClaimControllerSpec extends BaseSpringBootSpec {

    @Autowired
    CaseFactory factory

    @Autowired
    ClaimController controller

    def "A new case has a single claim"() {
        given:
        def response = factory.CreateCase().getBody()
        def claims = controller.getClaims(String.valueOf(response.getId()))
        ClaimController.Claim claim = claims[0]

        expect: "Case has single claim"
        claims.size() == 1
        claim.claimId > 0
        claim.lowerAmount == 1
        claim.higherAmount == 2
        claim.state == ClaimState.Issued
    }

    def "Confirm service for a claim"() {
        given:
        def userId = factory.createUser()
        def response = factory.CreateCase(userId).getBody()
        def claims = controller.getClaims(String.valueOf(response.getId()))
        def claim = claims[0]
        JsonNode data = new ObjectMapper().valueToTree(new ConfirmService("a", "user"));
        controller.createEvent((Long) claim.claimId, ClaimEvent.ConfirmService, data, userId)
        def modifiedClaim = controller.getClaims(String.valueOf(response.getId()))[0]
        ArrayList claimList = controller.getClaims(String.valueOf(response.getId()));
        ArrayList history = controller.getClaimEvents(String.valueOf(claim.claimId))

        expect:
        modifiedClaim.state == ClaimState.ServiceConfirmed
        claimList.size() == 1
        history.size() == 2
    }

    def "A claim has claimants and defendants"() {
        given:
        def response = factory.CreateCase().getBody()
        def claims = controller.getClaims(String.valueOf(response.getId()))
        def parties = claims[0].parties
        expect: "Case has single claim"
        parties.defendants.size() == 1
        parties.claimants.size() == 1
        parties.defendants[0].name == 'Wiki'
        parties.claimants[0].name == 'Hooli'
    }
}
