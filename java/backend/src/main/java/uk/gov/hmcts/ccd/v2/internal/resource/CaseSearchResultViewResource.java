package uk.gov.hmcts.ccd.v2.internal.resource;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import org.springframework.hateoas.RepresentationModel;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.SearchResultViewHeaderGroup;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.SearchResultViewItem;

import java.util.List;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class CaseSearchResultViewResource extends RepresentationModel {

    @Singular
    private List<SearchResultViewHeaderGroup> headers;
    @Singular
    private List<SearchResultViewItem> cases;
    private Long total;

}
