package org.example.api;

import org.example.model.CurrencyExchangeRequestBody;
import org.example.service.CommService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

    @Mock
    private CommService commServiceMock;
    @Captor
    private ArgumentCaptor<String> filtersCaptor;

    @Test
    void testGetCurrencies() {
        String expected = """
                {
                  "source":"bitcoin",
                  "rates":{
                    "usd": 67187.3358,
                    "eth": 21.37
                    }
                }""";
        String mockedThirdPartyResponse = """
                {
                  "bitcoin": {
                    "usd": 67187.3358,
                    "eth": 21.37
                    }
                }""";
        when(commServiceMock.callSimplePrice("bitcoin", "USD,ETH"))
                .thenReturn(mockedThirdPartyResponse);
        ResponseEntity<String> actual = new ApiController(commServiceMock).getCurrencies("bitcoin", List.of("USD,ETH"));
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void shouldUseDefaultFiltersWhenNoneArePassed() {
        when(commServiceMock.callSimplePrice(any(), any())).thenReturn("");
        new ApiController(commServiceMock).getCurrencies("bitcoin", Collections.emptyList());
        verify(commServiceMock).callSimplePrice(any(), filtersCaptor.capture());
        assertThat(filtersCaptor.getValue()).isEqualTo("USD,EUR,BTC,ETH,PLN");
    }

    @Test
    void testExchangeCurrency() {
        String expected = """
                {
                  "from":"bitcoin",
                   "usd": {
                    "rate":67187.3358,
                    "amount":12,
                    "result":814310.509896,
                    "fee":8062.480296
                    },
                    "eth": {
                    "rate":21.37,
                    "amount":12,
                    "result":259.0044,
                    "fee":2.5644
                    }
                }""";
        String mockedThirdPartyResponse = """
                {
                  "bitcoin": {
                    "usd": 67187.3358,
                    "eth": 21.37
                    }
                }""";
        when(commServiceMock.callSimplePrice("bitcoin", "USD,ETH"))
                .thenReturn(mockedThirdPartyResponse);
        CurrencyExchangeRequestBody requestBody = new CurrencyExchangeRequestBody("bitcoin", List.of("USD", "ETH"), new BigDecimal("12"));
        ResponseEntity<String> actual = new ApiController(commServiceMock).exchangeCurrency(requestBody);
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getBody()).isEqualToIgnoringWhitespace(expected);
    }

}