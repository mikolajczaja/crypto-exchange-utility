package org.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.NoResultsFoundException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public record GetCurrenciesThirdPartyResponse(String source, Map<String, BigDecimal> rates) {


    public static GetCurrenciesThirdPartyResponse fromJsonString(String jsonString, ObjectMapper objectMapper) throws JsonProcessingException, NoResultsFoundException {
        JsonNode nodes = objectMapper.readTree(jsonString);

        if (nodes.isEmpty()) {
            throw new NoResultsFoundException();
        }
        Map.Entry<String, JsonNode> next = nodes.fields().next();

        return new GetCurrenciesThirdPartyResponse(next.getKey(), objectMapper.convertValue(next.getValue(), new TypeReference<HashMap<String, BigDecimal>>() {
        }));
    }
}