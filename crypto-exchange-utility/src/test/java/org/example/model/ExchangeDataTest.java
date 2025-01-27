package org.example.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeDataTest {

    @Test
    void shouldCalculateCorrectly() {
        ExchangeData actual = new ExchangeData(new BigDecimal("0.67"), new BigDecimal("12"));
        assertThat(actual.getFee()).isEqualTo(new BigDecimal("0.0804"));
        assertThat(actual.getResult()).isEqualTo(new BigDecimal("8.1204"));
    }
}