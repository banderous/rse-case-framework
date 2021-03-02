package uk.gov.hmcts.ccd.domain.model.search.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.domain.model.search.CommonViewItem;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SearchResultViewItem implements CommonViewItem {

    @JsonProperty("case_id")
    private String caseId;
    private Map<String, Object> fields;
    @JsonProperty("fields_formatted")
    private Map<String, Object> fieldsFormatted;
    @JsonProperty("supplementary_data")
    private Map<String, JsonNode> supplementaryData;
}
