package ru.netology.money_transfer_service.models;

public class OperationId {
    private final String operationId;

    public OperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationId() {
        return operationId;
    }
}
