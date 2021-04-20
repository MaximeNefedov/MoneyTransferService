package ru.netology.money_transfer_service.models.transactions;

import ru.netology.money_transfer_service.exceptions.converter_exceptions.TransactionDataConverterException;
import ru.netology.money_transfer_service.models.cards.Amount;

public class TransactionDataBuilder {
    private final TransactionData transactionData = new TransactionData();

    public TransactionDataBuilder setCardFromNumber(String cardFromNumber) {
        transactionData.setCardFromNumber(cardFromNumber);
        return this;
    }

    public TransactionDataBuilder setCardToNumber(String cardToNumber) {
        transactionData.setCardToNumber(cardToNumber);
        return this;
    }

    public TransactionDataBuilder setCardFromValidTill(String cardFromValidTill) {
        transactionData.setCardFromValidTill(cardFromValidTill);
        return this;
    }

    public TransactionDataBuilder setCardFromCVV(String cardFromCVV) {
        transactionData.setCardFromCVV(cardFromCVV);
        return this;
    }

    public TransactionDataBuilder setAmount(Amount amount) {
        transactionData.setAmount(amount);
        return this;
    }

    public TransactionData build() {
        if (transactionData.allFieldsArePresent())
            throw new TransactionDataConverterException("Ошибка конвертирования. Все поля должны быть заполнены");
        return transactionData;
    }
}
