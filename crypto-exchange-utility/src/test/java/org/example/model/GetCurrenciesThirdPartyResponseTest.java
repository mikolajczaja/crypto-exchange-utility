package org.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.exception.NoResultsFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class GetCurrenciesThirdPartyResponseTest {

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
        GetCurrenciesThirdPartyResponse expected = new GetCurrenciesThirdPartyResponse("bitcoin",
                Map.of("usd", new BigDecimal("67187.3358"), "eth", new BigDecimal("21.37")));
        GetCurrenciesThirdPartyResponse actual = GetCurrenciesThirdPartyResponse.fromJsonString(inputString, objectMapper);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldThrowCorrectExceptionWhenJsonIsEmptyNodeWise() {
        assertThrows(NoResultsFoundException.class, () -> GetCurrenciesThirdPartyResponse.fromJsonString("{}", objectMapper));
    }
}