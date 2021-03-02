package uk.gov.hmcts.ccf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;

import java.util.Iterator;
import java.util.Map;

public class ESQueryParser {
    private ESQueryParser() {
    }

    @SneakyThrows
    public static XUIQuery parse(String json) {
        JsonNode node = new ObjectMapper().readTree(json);
        JsonNode query = node.get("native_es_query");
        XUIQuery result = new XUIQuery();
        result.setFrom(query.get("from").asInt());
        result.setPageSize(query.get("size").asInt());

        Map<String, String> params = Maps.newHashMap();
        result.setParams(params);
        for (JsonNode n : query.get("query").get("bool").get("must")) {
            JsonNode match = n.get("match");
            Iterator<String> fields = match.fieldNames();
            while (fields.hasNext()) {
                String fieldName = fields.next();
                String value = match.get(fieldName).get("query").textValue();
                params.put(fieldName.replaceFirst("data\\.", ""), value);
            }
        }

        return result;
    }

}
