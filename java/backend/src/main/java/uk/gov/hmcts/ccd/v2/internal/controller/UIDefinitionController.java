package uk.gov.hmcts.ccd.v2.internal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.definition.CaseTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.definition.FieldTypeDefinition;
import uk.gov.hmcts.ccd.domain.model.search.Field;
import uk.gov.hmcts.ccd.v2.V2;
import uk.gov.hmcts.ccd.v2.internal.resource.BannerViewResource;
import uk.gov.hmcts.ccd.v2.internal.resource.JurisdictionConfigViewResource;
import uk.gov.hmcts.ccd.v2.internal.resource.JurisdictionViewResource;
import uk.gov.hmcts.ccd.v2.internal.resource.SearchInputsViewResource;
import uk.gov.hmcts.ccd.v2.internal.resource.WorkbasketInputsViewResource;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/data/internal")
public class UIDefinitionController {
    @GetMapping(
        path = "/case-types/{caseTypeId}/work-basket-inputs",
        produces = {
            V2.MediaType.UI_WORKBASKET_INPUT_DETAILS
        }
    )
    public ResponseEntity<WorkbasketInputsViewResource> getWorkbasketInputsDetails(@PathVariable("caseTypeId")
                                                                                       String caseTypeId) {
        return ResponseEntity.ok(
            WorkbasketInputsViewResource.builder()
                .workbasketInput(
                    WorkbasketInputsViewResource.WorkbasketInputView.builder()
                        .label("Urgent Case")
                        .field(Field.builder()
                            .id("Urgent Case")
                            .type(
                                FieldTypeDefinition.builder()
                                    .id("YesOrNo")
                                    .type("YesOrNo")
                                    .build()
                            )
                            .build()
                        )
                        .build()
                ).build());
    }

    @GetMapping(
        path = "/case-types/{caseTypeId}/search-inputs",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.UI_SEARCH_INPUT_DETAILS
        }
    )
    public ResponseEntity<SearchInputsViewResource> getSearchInputsDetails(@PathVariable("caseTypeId")
                                                                                   String caseTypeId) {

        throw new RuntimeException();
    }

    @GetMapping(
        path = "/banners",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.UI_BANNERS
        }
    )
    public ResponseEntity<BannerViewResource> getBanners(@RequestParam("ids") Optional<List<String>> idsOptional) {
        throw new RuntimeException();
    }

    @GetMapping(
        path = "/jurisdiction-ui-configs",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.UI_JURISDICTION_CONFIGS
        }
    )
    public ResponseEntity<JurisdictionConfigViewResource> getJurisdictionUiConfigs(@RequestParam("ids")
                                                                                   Optional<List<String>> idsOptional) {
        throw new RuntimeException();
    }

    @GetMapping(
        path = "/jurisdictions",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.UI_JURISDICTIONS
        }
    )
    public ResponseEntity<JurisdictionViewResource> getJurisdictions(@RequestParam(value = "access") String access) {

        return ResponseEntity.ok(JurisdictionViewResource.builder().jurisdiction(
            JurisdictionViewResource.JurisdictionView.builder()
            .id("New Divorce")
            .name("New divorce jurisdiction")
            .description("New jurisdiction description")
                .caseTypeDefinition(CaseTypeDefinition.builder()
                    .id("NFD")
                    .name("No fault divorce")
                    .description("No Fault Divorce")
                    .build()
                )
            .build()
        ).build());
    }
}
