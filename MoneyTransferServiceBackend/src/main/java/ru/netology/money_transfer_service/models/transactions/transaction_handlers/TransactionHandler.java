package ru.netology.money_transfer_service.models.transactions.transaction_handlers;

import ru.netology.money_transfer_service.models.transactions.TransactionData;

public interface TransactionHandler {
    boolean checkingSendersCardData(TransactionData transactionData);
    boolean checkingCardToNumber(String numberToCard);
    String generateTransactionId();
}
