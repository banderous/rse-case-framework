package uk.gov.hmcts

import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import uk.gov.hmcts.ccf.controller.kase.ApiEventCreation
import uk.gov.hmcts.ccf.controller.kase.CaseController
import uk.gov.hmcts.unspec.dto.Company
import uk.gov.hmcts.unspec.dto.Organisation
import uk.gov.hmcts.unspec.event.CreateClaim

import static org.jooq.generated.Tables.USERS

@Component
class CaseFactory {

    @Autowired
    CaseController controller;

    @Autowired
    DefaultDSLContext jooq;

    String createUser(String id = "1") {
        jooq.insertInto(USERS, USERS.USER_ID, USERS.USER_FORENAME, USERS.USER_SURNAME)
                .values(id, "John", "Smith")
                .execute();
        return id;
    }

    ResponseEntity<CaseController.CaseActions> CreateCase(String userId = createUser()) {
        def event = CreateClaim.builder()
                .claimantReference("Foo")
                .defendantReference("Bar")
                .claimant(new Company("Hooli"))
                .defendant(new Organisation("Wiki"))
                .lowerValue(1)
                .higherValue(2)
                .build()
        def request = new ApiEventCreation("Create", new ObjectMapper().valueToTree(event))
        return controller.createCase(request, userId)
    }
}
