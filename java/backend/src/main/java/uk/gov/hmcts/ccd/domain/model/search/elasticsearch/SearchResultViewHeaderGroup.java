package uk.gov.hmcts.ccd.domain.model.search.elasticsearch;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.List;

@Builder
@Data
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SearchResultViewHeaderGroup {

    @NonNull
    private HeaderGroupMetadata metadata;
    @NonNull
    @Singular
    private List<SearchResultViewHeader> fields;
    @NonNull
    @Singular
    private List<String> cases;
}
