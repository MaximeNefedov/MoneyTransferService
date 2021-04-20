package ru.netology.money_transfer_service.exceptions;

public class TransactionCreationException extends RuntimeException {
    public TransactionCreationException(String message) {
        super(message);
    }
}
