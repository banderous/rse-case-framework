package uk.gov.hmcts.ccd.domain.model.aggregated;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CaseView extends AbstractCaseView {
    private ProfileCaseState state;
    private String[] channels;
    private List<CaseViewActionableEvent> actionableEvents;
    private CaseViewEvent[] caseViewEvents;

    public ProfileCaseState getState() {
        return state;
    }

    public void setState(ProfileCaseState state) {
        this.state = state;
    }

    public String[] getChannels() {
        return channels;
    }

    public void setChannels(String[] channels) {
        this.channels = channels;
    }

    @JsonProperty("triggers")
    public List<CaseViewActionableEvent> getActionableEvents() {
        return actionableEvents;
    }

    public void setActionableEvents(List<CaseViewActionableEvent> actionableEvents) {
        this.actionableEvents = actionableEvents;
    }

    @JsonProperty("events")
    public CaseViewEvent[] getEvents() {
        return caseViewEvents;
    }

    public void setEvents(CaseViewEvent[] events) {
        this.caseViewEvents = events;
    }
}
