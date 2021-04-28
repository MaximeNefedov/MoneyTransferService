package ru.netology.money_transfer_service.models.transactions;

import ru.netology.money_transfer_service.models.Currencies;
import ru.netology.money_transfer_service.models.cards.Amount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxCalculator {
    private static final BigDecimal percent = new BigDecimal("0.01");

    public static TaxValues calculateTaxes(BigDecimal base, Currencies currency) {
        final var percentValue = base.multiply(percent).setScale(2, RoundingMode.HALF_UP);
        final var valueAfterTax = base.subtract(percentValue);
        final var amountAfterTax = new Amount(currency, valueAfterTax.toString());
        final var percentAmount = new Amount(currency, percentValue.toString());
        return new TaxValues(amountAfterTax, percentAmount);
    }
}
