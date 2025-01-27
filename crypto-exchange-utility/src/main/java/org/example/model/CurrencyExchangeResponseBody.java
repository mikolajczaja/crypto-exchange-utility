package org.example.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.NoResultsFoundException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public record CurrencyExchangeResponseBody(String from, @JsonAnyGetter Map<String, ExchangeData> resultMap) {

    public static CurrencyExchangeResponseBody fromJsonString(String jsonString, ObjectMapper objectMapper, String from, BigDecimal amount) throws JsonProcessingException, NoResultsFoundException {
        JsonNode nodes = objectMapper.readTree(jsonString);

        if (nodes.isEmpty()) {
            throw new NoResultsFoundException();
        }
        Map.Entry<String, JsonNode> next = nodes.fields().next();

        CurrencyExchangeResponseBody responseBody = new CurrencyExchangeResponseBody(from, new HashMap<>());
        next.getValue().fields().forEachRemaining(entry -> responseBody.resultMap().put(entry.getKey(), new ExchangeData(objectMapper.convertValue(entry.getValue(), BigDecimal.class), amount)));

        return responseBody;
    }
}