package org.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.exception.NoResultsFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyExchangeResponseBodyTest {

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    void shouldCreateCorrectResponseObjectWhenPassingValidJsonInput() throws NoResultsFoundException, JsonProcessingException {
        String inputString = """
                {
                  "bitcoin": {
                    "usd": 67187.3358,
                    "eth": 21.37
                    }
                }""";
        String from = "bitcoin";
        BigDecimal amount = new BigDecimal("12.3");

        CurrencyExchangeResponseBody expected = new CurrencyExchangeResponseBody(from,
                Map.of("usd", new ExchangeData(new BigDecimal("67187.3358"), amount), "eth", new ExchangeData(new BigDecimal("21.37"), amount)));
        CurrencyExchangeResponseBody actual = CurrencyExchangeResponseBody.fromJsonString(inputString, objectMapper, from, amount);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldThrowCorrectExceptionWhenJsonIsEmptyNodeWise() {
        assertThrows(NoResultsFoundException.class, () -> CurrencyExchangeResponseBody.fromJsonString("{}", objectMapper, "bitcoin", new BigDecimal("10")));
    }
}