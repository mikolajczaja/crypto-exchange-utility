package org.example.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CurrencyExchangeRequestBody(@NotBlank String from, @NotEmpty List<String> to, @NotNull @Positive BigDecimal amount){
}
