package uk.gov.hmcts.ccd.v2.external.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.std.CaseDataContent;
import uk.gov.hmcts.ccd.v2.V2;
import uk.gov.hmcts.ccd.v2.external.resource.CaseResource;
import uk.gov.hmcts.ccf.StateMachine;
import uk.gov.hmcts.ccf.config.UserProvider;
import uk.gov.hmcts.unspec.statemachine.ClaimMachine;


import static org.springframework.http.ResponseEntity.status;

@RestController(value = "CCDController")
@RequestMapping(path = "/data")
public class CaseController {

    @Autowired
    ClaimMachine claimController;

    @Autowired
    DefaultDSLContext jooq;

    @Autowired
    UserProvider user;

    private Map<String, StateMachine> statemachines;

    @GetMapping(
        path = "/cases/{caseId}",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.CASE
        }
    )
    public ResponseEntity<CaseResource> getCase(@PathVariable("caseId") String caseId) {
        throw new RuntimeException();
    }

    @Autowired
    public CaseController(List<StateMachine> machines) {
        this.statemachines = Maps.newHashMap();
        for (StateMachine sm : machines) {
            statemachines.put(sm.id, sm);
        }
    }

    @SneakyThrows
    @PostMapping(
        path = "/cases/{caseId}/events"
    )
    @ResponseStatus(HttpStatus.CREATED) // To remove default 200 response from Swagger
    @SuppressWarnings("unchecked")
    @Transactional
    public ResponseEntity<CaseResource> createEvent(@PathVariable("caseId") long caseId,
                                                    @RequestBody final CaseDataContent content) {
        String json = new ObjectMapper().writeValueAsString(content.getData());
        JsonNode node = new ObjectMapper().readTree(json);

        String[] splits = content.getEventId().split("_");
        String machineId = splits[0];
        StateMachine<?, ? extends Enum<?>, ?> machine = statemachines.get(machineId);
        if (machine == null) {
            return status(HttpStatus.NOT_FOUND).body(CaseResource.builder().build());
        }

        String eventId = splits[1];
        // Entity refers to the case unless specified.
        long entityId = splits[0].equals("cases")
            ? caseId
            : Long.parseLong(splits[2]);

        StateMachine.TransitionContext context = new StateMachine.TransitionContext(
            user.getCurrentUserId(), entityId);

        machine.rehydrate(entityId);

        machine.handleEvent(context, eventId, node);

        CaseResource result = CaseResource.builder()
            .data(content.getData())
            .reference(content.getCaseReference())
            .jurisdiction("NFD")
            .state(machine.getState().toString())
            .build();
        return status(HttpStatus.CREATED).body(result);
    }

    @PostMapping(
        path = "/case-types/{caseTypeId}/cases",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.CREATE_CASE
        }
    )
    public ResponseEntity<CaseResource> createCase(@PathVariable("caseTypeId") String caseTypeId,
                                                   @RequestBody final CaseDataContent content) {
        throw new RuntimeException();
    }


}
