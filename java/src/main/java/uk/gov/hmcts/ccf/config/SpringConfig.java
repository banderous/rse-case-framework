package uk.gov.hmcts.ccf.config;

import lombok.With;
import org.jooq.generated.enums.CaseState;
import org.jooq.generated.enums.Event;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class SpringConfig
    extends EnumStateMachineConfigurerAdapter<CaseState, Event> {

    @Override
    public void configure(StateMachineStateConfigurer<CaseState, Event> states)
        throws Exception {
        states
            .withStates()
            .initial(CaseState.Created)
            .states(EnumSet.allOf(CaseState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<CaseState, Event> transitions)
        throws Exception {
        transitions
            .withExternal()
            .source(CaseState.Created).target(CaseState.Closed)
            .action(this::foo)
            .event(Event.CloseCase);
    }

    private void foo(StateContext<CaseState, Event> caseStateEventStateContext) {
        throw new RuntimeException();
    }

    public class StateMachineApplicationEventListener
        extends StateMachineListenerAdapter<CaseState, Event> {

        @Override
        public void stateMachineError(StateMachine<CaseState, Event> stateMachine, Exception exception) {
            throw new RuntimeException();
        }

    }

    @Bean
    public StateMachineApplicationEventListener contextListener() {
        return new StateMachineApplicationEventListener();
    }
}
