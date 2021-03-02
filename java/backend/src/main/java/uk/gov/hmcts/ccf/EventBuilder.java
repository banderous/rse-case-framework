package uk.gov.hmcts.ccf;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseUpdateViewEvent;
import uk.gov.hmcts.ccd.domain.model.aggregated.CaseViewField;
import uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.FixedListItemDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.WizardPage;
import uk.gov.hmcts.ccd.domain.model.definition.WizardPageField;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public class EventBuilder<T> {

    private Class<T> clazz;
    private CaseUpdateViewEvent.CaseUpdateViewEventBuilder builder = CaseUpdateViewEvent.builder();
    private Multimap<Integer, FieldInfo> fieldPageMap = ArrayListMultimap.create();
    private Map<Integer, String> pageLabels = Maps.newHashMap();
    private int currentPage = 1;
    private BiConsumer<Long, T> handler;
    private String showGroup;
    private static final ImmutableMap<Class, String> typeMap = ImmutableMap.<Class, String>builder()
        .put(Set.class, "MultiSelectList")
        .put(LocalDate.class, "Date")
        .put(String.class, "Text")
        .put(long.class, "Number")
        .put(Long.class, "Number")
        .put(boolean.class, "YesOrNo")
        .put(Boolean.class, "YesOrNo")
        .build();

    public static String typeName(Class c) {
        if (typeMap.containsKey(c)) {
            return typeMap.get(c);
        }
        return "Text";
    }

    public CaseUpdateViewEvent build() {
        for (int t = 1; t <= currentPage; t++) {
            Collection<FieldInfo> fieldNames =
                fieldPageMap.get(t);

            Collection<WizardPageField> fields = Lists.newArrayList();
            int i = 1;
            for (FieldInfo info : fieldNames) {
                fields.add(WizardPageField.builder()
                    .order(i++)
                    .caseFieldId(info.getId())
                    .pageColumnNumber(info.getColumn())
                    .build());
            }

            builder.wizardPage(WizardPage.builder()
                .order(t)
                .id(String.valueOf(t))
                .label(pageLabels.containsKey(t) ? pageLabels.get(t) : "Page " + t)
                .wizardPageFields(fields)
                .build());
        }
        return builder.build();
    }

    public EventBuilder(Class<T> clazz, String id, String label) {
        this.clazz = clazz;
        this.builder.id(id);
        this.builder.name(id);
        this.builder.description(id);
        this.pageLabels.put(currentPage, label);
    }

    public EventBuilder(Class<T> clazz) {
        this.clazz = clazz;
        this.builder.id("");
    }

    public EventBuilder<T> withHandler(BiConsumer<Long, T> handler) {
        this.handler = handler;
        return this;
    }

    public <U> EventBuilder<T> multiSelect(TypedPropertyGetter<T, ? extends Set<U>> getter,
                                           Map<U, String> options) {
        return multiSelect(getter, options, 1);

    }

    public <U> EventBuilder<T> multiSelect(TypedPropertyGetter<T, ? extends Set<U>> getter,
                                           Map<U, String> options, int column) {

        CaseViewField.CaseViewFieldBuilder fb = buildField(getter, "MultiSelectList");

        CaseViewField field = fb.build();
        List<FixedListItemDefinition> items = Lists.newArrayList();
        for (Map.Entry<U, String> entry : options.entrySet()) {
            FixedListItemDefinition.FixedListItemDefinitionBuilder ib = FixedListItemDefinition.builder();
            ib.code(entry.getKey().toString());
            ib.label(entry.getValue());
            items.add(ib.build());
        }
        field.setValue(Lists.newArrayList());
        field.getFieldTypeDefinition().setFixedListItemDefinitions(items);

        String id = PropertyUtils.getPropertyName(clazz, getter);
        fieldPageMap.put(currentPage, new FieldInfo(id, column, showGroup));

        builder.caseField(field);
        return this;
    }

    CaseViewField.CaseViewFieldBuilder buildField(TypedPropertyGetter<T, ?> getter, String type) {
        String id = PropertyUtils.getPropertyName(clazz, getter);

        FieldTypeDefinition.FieldTypeDefinitionBuilder ftb = FieldTypeDefinition.builder()
            .id(type)
            .type(type);

        XUI xui = PropertyUtils.getAnnotationOfProperty(this.clazz, getter, XUI.class);
        if (xui != null) {
            ftb.min(BigDecimal.valueOf(xui.min()));
            ftb.max(BigDecimal.valueOf(xui.max()));
            if (xui.type() != XUIType.Default) {
                ftb.type(xui.type().toString());
            }
        }

        return CaseViewField.builder()
            .id(id)
            .showSummaryChangeOption(true)
            .label(xui != null ? xui.label() : "")
            .showCondition(showGroup)
            .fieldTypeDefinition(ftb.build());
    }

    public EventBuilder<T> field(TypedPropertyGetter<T, ?> getter) {
        String id = PropertyUtils.getPropertyName(clazz, getter);
        PropertyDescriptor descriptor = de.cronn.reflection.util.PropertyUtils
            .getPropertyDescriptor(clazz, getter);
        Class propertyType = descriptor.getPropertyType();

        if (propertyType.isEnum()) {
            builder.caseField(CaseViewField.builder()
                .id(id)
                .showCondition(showGroup)
                .fieldTypeDefinition(buildFixedList(propertyType))
                .build());
        } else if (typeMap.containsKey(propertyType)) {
            builder.caseField(
                buildField(getter, typeMap.get(propertyType)).build()
            );
        } else {
            throw new RuntimeException("Unimplemented type:" + propertyType);
        }

        this.builder.showSummary(true);
        fieldPageMap.put(currentPage, new FieldInfo(id, 1, showGroup));

        return this;
    }

    private FieldTypeDefinition buildFixedList(Class propertyType) {
        FieldTypeDefinition.FieldTypeDefinitionBuilder fb = FieldTypeDefinition.builder();
        fb.id(UUID.randomUUID().toString())
            .type("FixedRadioList");

        int t = 1;
        List<FixedListItemDefinition> items = Lists.newArrayList();
        for (Object e : propertyType.getEnumConstants()) {
            FixedListItemDefinition.FixedListItemDefinitionBuilder ib = FixedListItemDefinition.builder();
            ib.code(e.toString());
            ib.order(String.valueOf(t++));
            if (e instanceof HasLabel) {
                ib.label(((HasLabel) e).getLabel());
            }
            items.add(ib.build());
        }
        fb.fixedListItemDefinitions(items);


        return fb.build();
    }

    public EventBuilder<T> nextPage() {
        currentPage++;
        return this;
    }

    public EventBuilder<T> showGroup(String showCondition) {
        this.showGroup = showCondition;
        return this;
    }

    @Data
    @AllArgsConstructor
    private class FieldInfo {
        private String id;
        private int column = 1;
        private String showCondition;
    }
}
