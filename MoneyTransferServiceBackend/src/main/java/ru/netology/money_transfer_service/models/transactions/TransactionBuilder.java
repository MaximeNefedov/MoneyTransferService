package ru.netology.money_transfer_service.models.transactions;

import ru.netology.money_transfer_service.exceptions.TransactionCreationException;
import ru.netology.money_transfer_service.logger.Logger;
import ru.netology.money_transfer_service.models.cards.Amount;

public class TransactionBuilder {
    private final Transaction transaction = new Transaction();
    private static final Logger LOGGER = Logger.getLogger();

    public TransactionBuilder setTransactionId(String transactionId) {
        transaction.setTransactionId(transactionId);
        return this;
    }

    public TransactionBuilder setCardFromNumber(String cardFromNumber) {
        transaction.setCardFromNumber(cardFromNumber);
        return this;
    }

    public TransactionBuilder setCardToNumber(String cardToNumber) {
        transaction.setCardToNumber(cardToNumber);
        return this;
    }

    public TransactionBuilder setAmount(Amount amount) {
        transaction.setAmount(amount);
        return this;
    }

    public TransactionBuilder setSecretCode(String secretCode) {
        transaction.setSecretCode(secretCode);
        return this;
    }

    public TransactionBuilder setPhoneNumber(String phoneNumber) {
        transaction.setPhoneNumber(phoneNumber);
        return this;
    }

    public TransactionBuilder setPercentValue(Amount percentAmount) {
        transaction.setPercentValue(percentAmount);
        return this;
    }

    public TransactionBuilder setValueAfterTax(Amount amountAfterTax) {
        transaction.setValueAfterTax(amountAfterTax);
        return this;
    }

    public Transaction build() {
        if (transaction.allFieldsArePresent()) {
            return transaction;
        } else {
            final var message = "Ошибка создания транзакции";
            LOGGER.log(message, true);
            throw new TransactionCreationException(message);
        }
    }
}
