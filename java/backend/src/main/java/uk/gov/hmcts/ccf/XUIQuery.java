package uk.gov.hmcts.ccf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class XUIQuery {
    Map<String, String> params;
    int from;
    int pageSize;
}
