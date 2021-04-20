package ru.netology.money_transfer_service.models.cards;

import ru.netology.money_transfer_service.models.Currencies;

import java.math.BigDecimal;
import java.util.Objects;

public class Amount {
    private final Currencies currency;
    private BigDecimal value;

    public Amount(Currencies currency, String value) {
        this.currency = currency;
        this.value = new BigDecimal(value);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currencies getCurrency() {
        return currency;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value + " " + currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Amount amount = (Amount) o;
        return Objects.equals(currency, amount.currency) && Objects.equals(value, amount.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, value);
    }
}
