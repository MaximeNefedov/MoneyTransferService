package ru.netology.money_transfer_service.exceptions.converter_exceptions;

public class TransactionDataConverterException extends RuntimeException {
    public TransactionDataConverterException(String message) {
        super(message);
    }
}
