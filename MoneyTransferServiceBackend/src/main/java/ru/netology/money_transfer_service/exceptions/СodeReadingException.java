package ru.netology.money_transfer_service.exceptions;

public class СodeReadingException extends RuntimeException {
    public СodeReadingException(String message) {
        super(message);
    }
}
