package uk.gov.hmcts.unspec.dto;

import uk.gov.hmcts.ccf.HasLabel;

public enum PartyType implements HasLabel {
    Company("Company"),
    SoleTrader("Sole trader"),
    Individual("Individual");

    private final String val;

    PartyType(String val) {
        this.val = val;
    }

    @Override
    public String getLabel() {
        return val;
    }
}
