package ru.netology.money_transfer_service.exceptions;

public class WrongRecipientCardNumber extends RuntimeException {
    public WrongRecipientCardNumber(String message) {
        super(message);
    }
}
