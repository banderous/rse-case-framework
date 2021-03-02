package uk.gov.hmcts.ccf;

import com.google.common.collect.Lists;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTab;

import java.util.List;

public class CaseViewBuilder {
    private List<CaseViewTab> tabs = Lists.newArrayList();
    private int order = 0;

    public CaseViewBuilder newTab(CaseViewTab tab) {
        this.tabs.add(tab);
        return this;
    }

    public TabBuilder newTab(String id, String label) {
        CaseViewTab caseViewTab = new CaseViewTab();
        caseViewTab.setOrder(order++);
        caseViewTab.setId(id);
        caseViewTab.setLabel(label);
        tabs.add(caseViewTab);
        return new TabBuilder(this, caseViewTab);
    }

    public List<CaseViewTab> build() {
        return this.tabs;
    }
}

