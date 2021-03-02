package uk.gov.hmcts.ccf;

import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewTab;
import uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition;

import java.rmi.server.UID;

public class TabBuilder {

    private final CaseViewTab tab;
    private final CaseViewBuilder parent;

    public TabBuilder(CaseViewBuilder parent, CaseViewTab tab) {
        this.parent = parent;
        this.tab = tab;
    }

    public TabBuilder label(String label) {
        return field(label, null, null, "Label");
    }

    public TabBuilder textField(String label, String value, String hint) {
        return field(label, value, hint, "Text");
    }

    public TabBuilder field(String label, Object value, String hint, String type) {
        CaseViewField f = CaseViewField.builder()
            .id(new UID().toString())
            .value(value)
            .hintText(hint)
            .label(label)
            .order(1)
            .formattedValue(value)
            .fieldTypeDefinition(type(type)).build();
        tab.getFields().add(f);
        return this;
    }

    private FieldTypeDefinition type(String type) {
        return FieldTypeDefinition.builder()
            .id(type)
            .type(type)
            .build();
    }

    public CaseViewBuilder build() {
        return this.parent;
    }
}
