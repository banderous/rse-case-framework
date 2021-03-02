package uk.gov.hmcts.ccd.v2.external.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ccd.domain.model.std.CaseDataContent;
import uk.gov.hmcts.ccd.v2.V2;
import uk.gov.hmcts.ccd.v2.external.resource.CaseDataResource;

@RestController
@RequestMapping(path = "/data/case-types")
public class CaseDataValidatorController {

    @PostMapping(
        path = "/{caseTypeId}/validate",
        headers = {
            V2.EXPERIMENTAL_HEADER
        },
        produces = {
            V2.MediaType.CASE_DATA_VALIDATE
        }
    )
    public ResponseEntity<CaseDataResource> validate(@PathVariable("caseTypeId") String caseTypeId,
                                                     @RequestParam(required = false) final String pageId,
                                                     @RequestBody final CaseDataContent content) {

        return ResponseEntity.ok(new CaseDataResource(content, caseTypeId, pageId));
    }
}
