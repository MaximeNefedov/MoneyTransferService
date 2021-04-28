package ru.netology.money_transfer_service.models.transactions;

import ru.netology.money_transfer_service.models.cards.Amount;

public class TaxValues {
    private final Amount amountAfterTax;
    private final Amount percentAmount;

    public TaxValues(Amount amountAfterTax, Amount percentAmount) {
        this.amountAfterTax = amountAfterTax;
        this.percentAmount = percentAmount;
    }

    public Amount getAmountAfterTax() {
        return amountAfterTax;
    }

    public Amount getPercentAmount() {
        return percentAmount;
    }
}
