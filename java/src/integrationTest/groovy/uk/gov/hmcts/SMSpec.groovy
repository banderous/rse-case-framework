package uk.gov.hmcts

import org.jooq.generated.enums.CaseState
import org.jooq.generated.enums.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.access.StateMachineAccess
import org.springframework.statemachine.access.StateMachineFunction
import org.springframework.statemachine.support.StateMachineInterceptorAdapter
import spock.lang.Specification

@SpringBootTest
class SMSpec extends Specification {

    @Autowired
    StateMachine<CaseState, Event> stateMachine;

    def "it works"() {
        when:
        stateMachine.start();
        def id = Thread.currentThread().getId();
        def res = stateMachine.sendEvent(Event.CloseCase);
        def bar = stateMachine.getExtendedState();
        def baz = stateMachine.hasStateMachineError();;

        then:
        stateMachine.hasStateMachineError() == true
        stateMachine.getState().getId() == CaseState.Closed

    }
}
