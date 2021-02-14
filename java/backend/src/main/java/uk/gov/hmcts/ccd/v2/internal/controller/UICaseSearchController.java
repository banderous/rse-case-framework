package uk.gov.hmcts.ccd.v2.internal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.HeaderGroupMetadata;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.SearchResultViewHeader;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.SearchResultViewHeaderGroup;
import uk.gov.hmcts.ccd.domain.model.search.elasticsearch.SearchResultViewItem;
import uk.gov.hmcts.ccd.v2.internal.resource.CaseSearchResultViewResource;
import uk.gov.hmcts.ccf.ColumnMapper;
import uk.gov.hmcts.ccf.ESQueryParser;
import uk.gov.hmcts.ccf.EventBuilder;
import uk.gov.hmcts.ccf.XUISearchHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/data/internal/searchCases", consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class UICaseSearchController {

    @Autowired
    XUISearchHandler<?> handler;

    @PostMapping(path = "")
    public ResponseEntity<CaseSearchResultViewResource> searchCases(
                                     @RequestParam(value = "ctid") String caseTypeId,
                                     @RequestParam(value = "use_case", required = false) final String useCase,
                                     @RequestBody String jsonSearchRequest) {

        ESQueryParser.ESQuery query = ESQueryParser.parse(jsonSearchRequest);
        Collection<? extends XUISearchHandler.XUISearchResult> results = handler.search(query);

        List<SearchResultViewItem> cases = new ArrayList<>();
        for (XUISearchHandler.XUISearchResult result : results) {
            cases.add(SearchResultViewItem.builder()
                .caseId(result.getCaseId().toString())
                .fields(new ObjectMapper().convertValue(result, Map.class))
                .build());
        }


        return ResponseEntity.ok(CaseSearchResultViewResource.builder()
            .cases(cases)
            .total(110000L)
            .header(SearchResultViewHeaderGroup.builder()
                .metadata(HeaderGroupMetadata.builder()
                    .jurisdiction("DIVORCE")
                    .caseTypeId("NFD")
                    .build())
                .fields(buildColumns())
                .build())
            .build());
    }

    private List<SearchResultViewHeader> buildColumns() {
        List<SearchResultViewHeader> result = Lists.newArrayList();
        handler.configureColumns(new ColumnMapper() {

            @Override
            public ColumnMapper column(TypedPropertyGetter getter) {
                Class c = handler.fieldType(getter);
                String type = EventBuilder.typeName(c);
                result.add(SearchResultViewHeader.builder()
                    .caseFieldId(handler.fieldName(getter))
                    .label(handler.fieldName(getter))
                    .caseFieldTypeDefinition(FieldTypeDefinition.builder()
                        .id(type)
                        .type(type)
                        .build())
                    .build());
                return this;
            }
        });
        return result;
    }
}
