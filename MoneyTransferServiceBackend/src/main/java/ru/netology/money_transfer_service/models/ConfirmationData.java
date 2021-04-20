package ru.netology.money_transfer_service.models;

public class ConfirmationData {
    private final String operationId;
    private String code;

    public ConfirmationData(String operationId, String code) {
        this.operationId = operationId;
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "Confirmation{" +
                "operationId='" + operationId + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
