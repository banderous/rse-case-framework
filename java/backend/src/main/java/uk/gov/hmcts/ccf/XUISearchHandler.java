package uk.gov.hmcts.ccf;

import de.cronn.reflection.util.PropertyUtils;
import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.jodah.typetools.TypeResolver;

import java.beans.PropertyDescriptor;
import java.util.Collection;

public abstract class XUISearchHandler<S extends XUISearchHandler.XUISearchResult> {

    public String fieldLabel(TypedPropertyGetter<S, ?> getter) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(XUISearchHandler.class, this.getClass());
        Class<S> clazz = (Class<S>) typeArgs[0];

        XUI xui = PropertyUtils.getAnnotationOfProperty(clazz, getter, XUI.class);
        if (null != xui) {
            return xui.label();
        }
        return "";
    }

    public String fieldName(TypedPropertyGetter<S, ?> getter) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(XUISearchHandler.class, this.getClass());
        Class<S> clazz = (Class<S>) typeArgs[0];

        return PropertyUtils.getPropertyName(clazz, getter);
    }

    public Class fieldType(TypedPropertyGetter<S, ?> getter) {
        Class<?>[] typeArgs = TypeResolver.resolveRawArguments(XUISearchHandler.class, this.getClass());
        Class<S> clazz = (Class<S>) typeArgs[0];
        PropertyDescriptor descriptor = de.cronn.reflection.util.PropertyUtils
            .getPropertyDescriptor(clazz, getter);
        return descriptor.getPropertyType();
    }

    public abstract void configureWorkbasketInputs(WorkbasketInputBuilder builder);

    public abstract void configureColumns(ColumnMapper<S> mapper);

    public abstract SearchResults search(XUIQuery query);

    public interface XUISearchResult {
        Long getCaseId();
    }

    @Getter
    @AllArgsConstructor
    public static class SearchResults {
        private long rowCount;
        private Collection<? extends XUISearchResult> results;
    }
}
