package ru.netology.money_transfer_service.exceptions;

public class IncorrectVerificationCode extends RuntimeException {
    public IncorrectVerificationCode(String message) {
        super(message);
    }
}
