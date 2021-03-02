package uk.gov.hmcts.ccf;

import de.cronn.reflection.util.TypedPropertyGetter;

public interface ColumnMapper<T> {
    ColumnMapper<T> column(TypedPropertyGetter<T, ?> getter);
}
