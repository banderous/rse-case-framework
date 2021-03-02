package uk.gov.hmcts.ccf.controller.kase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiEventCreation {
    private String id;
    private JsonNode data;

    public ApiEventCreation(Object id, Object o) {
        this.id = id.toString();
        this.data = new ObjectMapper().valueToTree(o);
    }
}
