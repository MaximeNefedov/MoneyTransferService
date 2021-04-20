package ru.netology.money_transfer_service.exceptions;

public class IncorrectSenderCardData extends RuntimeException {
    public IncorrectSenderCardData(String message) {
        super(message);
    }
}
