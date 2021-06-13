package uk.gov.hmcts

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Sets
import org.jooq.generated.enums.CaseState
import org.jooq.generated.enums.Event
import org.jooq.generated.tables.records.EventsRecord
import org.jooq.impl.DefaultDSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import uk.gov.hmcts.ccf.StateMachine
import uk.gov.hmcts.unspec.statemachine.CaseMachine
import uk.gov.hmcts.unspec.dto.Company
import uk.gov.hmcts.unspec.dto.Organisation
import uk.gov.hmcts.unspec.event.CreateClaim

import static org.jooq.generated.Tables.USERS

@Component
class CaseFactory {

    @Autowired
    StateMachine<CaseState, Event, EventsRecord> machine;

    @Autowired
    DefaultDSLContext jooq;

    String createUser(String id = "1") {
        jooq.insertInto(USERS, USERS.USER_ID, USERS.USER_FORENAME, USERS.USER_SURNAME)
                .values(id, "John", "Smith")
                .execute();
        return id;
    }

    long CreateCase(String userId = createUser()) {
        def event = CreateClaim.builder()
                .claimantReference("Foo")
                .defendantReference("Bar")
                .claimant(new Company("Hooli"))
                .defendant(new Organisation("Wiki"))
                .lowerValue(1)
                .higherValue(2)
                .build()
        return machine.onCreated(userId, new ObjectMapper().valueToTree(event))
    }
}
