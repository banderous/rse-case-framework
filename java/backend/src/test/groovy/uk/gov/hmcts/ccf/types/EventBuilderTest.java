package uk.gov.hmcts.ccf.types;

import org.apache.groovy.util.Maps;
import org.junit.Test;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseUpdateViewEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.FixedListItemDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.WizardPage;
import uk.gov.hmcts.ccd.domain.model.definition.WizardPageField;
import uk.gov.hmcts.ccf.EventBuilder;
import uk.gov.hmcts.ccf.types.dto.GeneralReferral;
import uk.gov.hmcts.unspec.dto.AddClaim;
import uk.gov.hmcts.unspec.dto.AddParty;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class EventBuilderTest {

    @Test
    public void buildReasonForReferral() {
        EventBuilder<GeneralReferral> e = new EventBuilder<>(GeneralReferral.class);
        e.field(GeneralReferral::getReason);
        FieldTypeDefinition fieldtype = e.build().getCaseFields().get(0).getFieldTypeDefinition();

        assertEquals("FixedRadioList", fieldtype.getType());
        assertEquals(2, fieldtype.getFixedListItemDefinitions().size());
        assertEquals("Caseworker referral", fieldtype.getFixedListItemDefinitions().get(0).getLabel());
    }

    @Test
    public void buildsReferralDate() {
        EventBuilder<GeneralReferral> e = new EventBuilder<>(GeneralReferral.class);
        e.field(GeneralReferral::getDate);
        CaseViewField date = e.build().getCaseFields().get(0);

        assertEquals("date", date.getId());
        assertTrue(date.getShowSummaryChangeOption());
        assertEquals("Application or referral date", date.getLabel());

        FieldTypeDefinition fieldtype = date.getFieldTypeDefinition();
        assertEquals("Date", fieldtype.getType());
        assertEquals("Date", fieldtype.getId());
    }

    @Test
    public void buildsReferralDetails() {
        EventBuilder<GeneralReferral> e = new EventBuilder<>(GeneralReferral.class);
        e.field(GeneralReferral::getReferralDetails);
        CaseViewField date = e.build().getCaseFields().get(0);
        FieldTypeDefinition fieldtype = date.getFieldTypeDefinition();

        assertEquals("referralDetails", date.getId());

        assertEquals("Text", fieldtype.getType());
        assertEquals("Text", fieldtype.getId());
    }

    @Test
    public void buildsBoolean() {
        EventBuilder<GeneralReferral> e = new EventBuilder<>(GeneralReferral.class);
        e.field(GeneralReferral::isFeeRequired);
        CaseViewField date = e.build().getCaseFields().get(0);
        FieldTypeDefinition fieldtype = date.getFieldTypeDefinition();

        assertEquals("feeRequired", date.getId());

        assertEquals("YesOrNo", fieldtype.getType());
        assertEquals("YesOrNo", fieldtype.getId());
    }

    @Test
    public void buildsWizardPage() {
        EventBuilder<GeneralReferral> e = new EventBuilder<>(GeneralReferral.class);
        e.field(GeneralReferral::isFeeRequired);
        e.field(GeneralReferral::getDate);
        List<WizardPage> pages = e.build().getWizardPages();
        assertEquals(1, pages.size());
        WizardPage page = pages.get(0);
        assertEquals("1", page.getId());
        assertEquals(1, page.getOrder());
        assertEquals(2, page.getWizardPageFields().size());

        List<WizardPageField> fields = page.getWizardPageFields();
        assertEquals("feeRequired", fields.get(0).getCaseFieldId());
        assertEquals("date", fields.get(1).getCaseFieldId());

        assertEquals(1, fields.get(0).getOrder());
        assertEquals(2, fields.get(1).getOrder());
    }

    @Test
    public void buildsWizardPages() {
        EventBuilder<GeneralReferral> e = new EventBuilder<>(GeneralReferral.class);
        e.field(GeneralReferral::isFeeRequired);
        e.nextPage();
        e.field(GeneralReferral::getDate);
        List<WizardPage> pages = e.build().getWizardPages();
        assertEquals(2, pages.size());
        WizardPage page = pages.get(0);
        assertEquals(1, page.getOrder());
        assertEquals(1, page.getWizardPageFields().size());

        List<WizardPageField> fields = page.getWizardPageFields();
        assertEquals("feeRequired", fields.get(0).getCaseFieldId());

        page = pages.get(1);
        fields = page.getWizardPageFields();
        assertEquals("date", fields.get(0).getCaseFieldId());
        assertEquals(1, fields.get(0).getOrder());
    }

    @Test
    public void buildsMultiSelect() {
        EventBuilder<AddClaim> e = new EventBuilder<>(AddClaim.class);
        Map<Long, String> labels = Maps.of(1L, "foo", 2L, "bar");
        e.multiSelect(AddClaim::getClaimants, labels);
        e.multiSelect(AddClaim::getDefendants, labels);

        CaseUpdateViewEvent result = e.build();
        CaseViewField select = result.getCaseFields().get(0);
        FieldTypeDefinition fieldtype = select.getFieldTypeDefinition();

        assertEquals("claimants", select.getId());
        assertTrue(List.class.isAssignableFrom(select.getValue().getClass()));
        assertEquals(0, fieldtype.getComplexFields().size());
        assertEquals("MultiSelectList", fieldtype.getType());
        assertEquals("Select claimants", select.getLabel());

        assertEquals(2, fieldtype.getFixedListItemDefinitions().size());
        List<FixedListItemDefinition> defs =
            fieldtype.getFixedListItemDefinitions();

        assertEquals("1", defs.get(0).getCode());
        assertEquals("foo", defs.get(0).getLabel());

        assertEquals("2", defs.get(1).getCode());
        assertEquals("bar", defs.get(1).getLabel());

        List<WizardPage> pages = result.getWizardPages();
        assertEquals(1, pages.size());
        assertEquals(2, pages.get(0).getWizardPageFields().size());
    }

    @Test
    public void buildsNumbers() {
        EventBuilder<AddClaim> e = new EventBuilder<>(AddClaim.class);
        e.field(AddClaim::getLowerValue);

        CaseUpdateViewEvent result = e.build();
        CaseViewField select = result.getCaseFields().get(0);
        FieldTypeDefinition fieldtype = select.getFieldTypeDefinition();

        assertEquals("MoneyGBP", fieldtype.getType());
        assertEquals(0, fieldtype.getMin().longValue());
        assertEquals(Long.MAX_VALUE, fieldtype.getMax().longValue());
    }

    @Test
    public void buildsShowgroups() {
        EventBuilder<AddParty> e = new EventBuilder<>(AddParty.class);
        String showCondition = "foo = \"bar\"";
        e.field(AddParty::getName)
            .showGroup(showCondition)
            .field(AddParty::getFirstName)
            .field(AddParty::getLastName);

        CaseUpdateViewEvent result = e.build();
        assertNull(result.getCaseFields().get(0).getShowCondition());
        assertEquals(showCondition, result.getCaseFields().get(1).getShowCondition());
        assertEquals(showCondition, result.getCaseFields().get(2).getShowCondition());
    }
}
