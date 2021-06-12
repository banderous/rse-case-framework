package uk.gov.hmcts.ccd.v2.internal.controller;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseUpdateViewEvent;
import uk.gov.hmcts.ccd.v2.V2;
import uk.gov.hmcts.ccd.v2.internal.resource.CaseUpdateViewEventResource;
import uk.gov.hmcts.ccf.StateMachine;
import uk.gov.hmcts.ccf.config.UserProvider;

@RestController
@RequestMapping(path = "/data/internal")
public class UIStartTriggerController {
    private static final String ERROR_CASE_ID_INVALID = "Case ID is not valid";

    private Map<String, StateMachine> stateMachines;

    @Autowired
    UserProvider user;

    @Autowired
    public UIStartTriggerController(List<StateMachine> machines) {
        this.stateMachines = Maps.newHashMap();
        for (StateMachine machine : machines) {
            stateMachines.put(machine.id, machine);
        }
    }

    @GetMapping(
        path = "/case-types/{caseTypeId}/event-triggers/{triggerId}",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.CASE_TYPE_UPDATE_VIEW_EVENT
        }
    )
    public ResponseEntity<CaseUpdateViewEventResource> getCaseUpdateViewEventByCaseType(@PathVariable("caseTypeId")
                                                                                                String caseTypeId,
                                                                                        @PathVariable("triggerId")
                                                                                            String triggerId,
                                                               @RequestParam(value = "ignore-warning", required = false)
                                                                                          final Boolean ignoreWarning) {

        throw new RuntimeException();
    }

    @GetMapping(
        path = "/cases/{caseId}/event-triggers/{triggerId}",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.CASE_UPDATE_VIEW_EVENT
        }
    )
    public ResponseEntity<CaseUpdateViewEventResource> getCaseUpdateViewEvent(@PathVariable("caseId") long caseId,
                                                                            @PathVariable("triggerId") String triggerId,
                                                                            @RequestParam(value = "ignore-warning",
                                                                                required = false)
                                                                                  final Boolean ignoreWarning) {
        String[] splits = triggerId.split("_");
        String machineId = splits[0];
        String eventId = splits[1];

        StateMachine<?, ? extends Enum<?>, ?> machine = stateMachines.get(machineId);
        long entityId = machineId.equals("cases")
            ? caseId
            : Long.parseLong(splits[2]);
        String param = splits.length > 2 ? splits[2] : null;
        machine.rehydrate(entityId);
        CaseUpdateViewEvent view = machine.getEvent(user.getCurrentUserId(), entityId, eventId, param);

        CaseUpdateViewEventResource e = CaseUpdateViewEventResource.forCase(
            view,
            String.valueOf(caseId),
            ignoreWarning);
        e.getCaseUpdateViewEvent().setCaseId(String.valueOf(caseId));
        e.getCaseUpdateViewEvent().setId(triggerId);
        return ResponseEntity.ok(e);
    }

    @GetMapping(
        path = "/drafts/{draftId}/event-trigger",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.UI_START_DRAFT_TRIGGER
        }
    )
    public ResponseEntity<CaseUpdateViewEventResource> getStartDraftTrigger(@PathVariable("draftId") String draftId,
                                                                            @RequestParam(value = "ignore-warning",
                                                                                required = false)
                                                                            final Boolean ignoreWarning) {

        throw new RuntimeException();
    }
}
