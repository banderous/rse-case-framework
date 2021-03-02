package uk.gov.hmcts.ccf.types;

import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.junit.Test;
import uk.gov.hmcts.ccf.ESQueryParser;
import uk.gov.hmcts.ccf.XUIQuery;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class QueryParserTest {

    @SneakyThrows
    @Test
    public void buildReasonForReferral() {
        URL url = Resources.getResource("requests/data/internal/searchCases.json");
        String json = Resources.toString(url, StandardCharsets.UTF_8);

        XUIQuery query = ESQueryParser.parse(json);

        assertEquals(50, query.getFrom());
        assertEquals(25, query.getPageSize());
        Map<String, String> expectedMap = Map.of(
            "state", "AwaitingPayment",
            "D8DivorceUnit", "serviceCentre",
            "SolUrgentCase", "Yes"
        );
        assertEquals(expectedMap, query.getParams());
    }

}
