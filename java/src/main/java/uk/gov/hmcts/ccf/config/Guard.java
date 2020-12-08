package uk.gov.hmcts.ccf.config;

import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
public class Guard {

    @OnTransition
    public void onClosed() {
        throw new RuntimeException();
    }
}

