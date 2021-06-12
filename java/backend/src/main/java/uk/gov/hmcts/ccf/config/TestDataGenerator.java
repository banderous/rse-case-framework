package uk.gov.hmcts.ccf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.jooq.generated.enums.CaseState;
import org.jooq.generated.enums.Event;
import org.jooq.generated.tables.records.EventsRecord;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccf.StateMachine;
import uk.gov.hmcts.unspec.dto.AddClaim;
import uk.gov.hmcts.unspec.dto.AddParty;
import uk.gov.hmcts.unspec.dto.Address;
import uk.gov.hmcts.unspec.dto.Company;
import uk.gov.hmcts.unspec.dto.Organisation;
import uk.gov.hmcts.unspec.dto.PartyType;
import uk.gov.hmcts.unspec.event.CreateClaim;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.jooq.generated.Tables.EVENTS;
import static org.jooq.generated.Tables.USERS;
import static org.jooq.impl.DSL.count;

@Component
public class TestDataGenerator implements Callback {

    @Autowired
    private StateMachine<CaseState, Event, EventsRecord> controller;

    @Value("${generate-data:true}")
    public String generate;

    @Value("${big-ids:true}")
    public String bigCaseIds;

    @Autowired
    DefaultDSLContext create;

    @Override
    @SneakyThrows
    public void handle(org.flywaydb.core.api.callback.Event event, Context context) {
        if (!"true".equalsIgnoreCase(bigCaseIds)) {
            return;
        }
        int count = create.select(count()).from(EVENTS).fetchSingle().value1();
        if (count > 0) {
            return;
        }
        if ("true".equalsIgnoreCase(bigCaseIds)) {
            create.execute("ALTER SEQUENCE cases_case_id_seq RESTART WITH 2542345663454321;");
        }
        // User 'john' in the idam configuration.
        String testUserId = "super@gmail.com";
        create.insertInto(USERS,USERS.USER_ID, USERS.USER_FORENAME, USERS.USER_SURNAME)
            .values(testUserId, "John", "Smith")
            .execute();

        Address address = Address.builder()
            .address1("A house")
            .address2("A street")
            .address3("A city")
            .postcode("ZX8 4PQ").build();

        Company acme = new Company("Acme Ltd");
        acme.setAddress(address);
        Organisation megacorp = new Organisation("Megacorp Inc");
        megacorp.setAddress(address);
        CreateClaim o = CreateClaim.builder()
            .claimantReference("666")
            .defendantReference("999")
            .lowerValue(5000)
            .higherValue(10000)
            .claimant(acme)
            .defendant(megacorp)
            .build();
        Long caseId = controller.onCreated(testUserId, new ObjectMapper().valueToTree(o));

        AddParty ap = AddParty.builder()
            .partyType(PartyType.Individual)
            .title("Hooli Inc")
            .address(address)
            .firstName("")
            .lastName("")
            .build();

        controller.handleEvent(testUserId, caseId, Event.AddParty, new ObjectMapper().valueToTree(ap));

        ap = AddParty.builder()
            .partyType(PartyType.Individual)
            .title("Flixnet inc")
            .address(address)
            .firstName("")
            .lastName("")
            .build();

        controller.handleEvent(testUserId, caseId, Event.AddParty, new ObjectMapper().valueToTree(ap));

        AddClaim a = AddClaim.builder()
                .defendants(Set.of(1L))
                .claimants(Set.of(2L, 3L, 4L))
                .lowerValue(10000)
                .higherValue(100000)
                .build();
        controller.handleEvent(testUserId, caseId, Event.AddClaim, new ObjectMapper().valueToTree(a));

        o = CreateClaim.builder()
                .claimantReference("1111")
                .defendantReference("33333")
                .lowerValue(1000000)
                .higherValue(100000000)
                .claimant(new Company("Hooli"))
                .defendant(new Organisation("Acme Inc"))
                .build();
        controller.onCreated(testUserId, new ObjectMapper().valueToTree(o));
        controller.handleEvent(testUserId, caseId, Event.AddClaim, new ObjectMapper().valueToTree(a));

        URL url = Resources.getResource("seed_data/seed.sql");
        String sql = Resources.toString(url, StandardCharsets.UTF_8);
        create.execute(sql);
    }

    @Override
    public String getCallbackName() {
        return "Seed demo data";
    }

    @Override
    public boolean supports(org.flywaydb.core.api.callback.Event event, Context context) {
        return event == org.flywaydb.core.api.callback.Event.AFTER_MIGRATE;
    }

    @Override
    public boolean canHandleInTransaction(org.flywaydb.core.api.callback.Event event, Context context) {
        return true;
    }

}
