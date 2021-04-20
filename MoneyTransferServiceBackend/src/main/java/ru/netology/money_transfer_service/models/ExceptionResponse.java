package ru.netology.money_transfer_service.models;

public class ExceptionResponse {
    private final String message;
    private final int id;

    public ExceptionResponse(String message, int id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public int getId() {
        return id;
    }
}
