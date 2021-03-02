package uk.gov.hmcts.ccf.types.dto;

import uk.gov.hmcts.ccf.HasLabel;

public enum ReferralReason implements HasLabel {
    CaseWorkerReferral("Caseworker referral"),
    GeneralApplicationReferral("General application referral");

    private String label;
    ReferralReason(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
