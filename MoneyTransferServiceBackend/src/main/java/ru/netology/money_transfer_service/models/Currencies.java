package ru.netology.money_transfer_service.models;

public enum Currencies {
    RUR("RUR"), EURO("EURO");

    String description;
    Currencies(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
