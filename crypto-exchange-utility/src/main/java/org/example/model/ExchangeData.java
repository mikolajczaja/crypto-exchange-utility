package org.example.model;

import lombok.Data;

import java.math.BigDecimal;

//customization points: precision
@Data
public final class ExchangeData {
    private final BigDecimal rate;
    private final BigDecimal amount;
    /**
     * total (100% + 1% of fee) cost of exchange (rate x amount)
     */
    private final BigDecimal result;
    /**
     * 1% of exchange cost (rate x amount)
     */
    private final BigDecimal fee;

    public ExchangeData(BigDecimal rate, BigDecimal amount) {
        this.rate = rate;
        this.amount = amount;
        BigDecimal withoutFee = amount.multiply(rate);
        this.fee = withoutFee.multiply(BigDecimal.valueOf(0.01));
        this.result = withoutFee.add(fee);
    }
}