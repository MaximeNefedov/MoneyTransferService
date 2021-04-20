package ru.netology.money_transfer_service.models.transactions;

import ru.netology.money_transfer_service.models.cards.Amount;

public class Transaction {
    private String transactionId;
    private String cardFromNumber;
    private String cardToNumber;
    private Amount amount;
    private String secretCode;
    private String phoneNumber;
    private Amount amountAfterTax;
    private Amount percentAmount;
    private boolean isCompleted;

    public boolean allFieldsArePresent() {
        return transactionId != null &&
                cardFromNumber != null &&
                cardToNumber != null &&
                amount != null &&
                secretCode != null &&
                phoneNumber != null &&
                amountAfterTax != null &&
                percentAmount != null;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setValueAfterTax(Amount amountAfterTax) {
        this.amountAfterTax = amountAfterTax;
    }

    public void setPercentValue(Amount percentAmount) {
        this.percentAmount = percentAmount;
    }

    public void setCompletedStatus() {
        isCompleted = true;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getCardFromNumber() {
        return cardFromNumber;
    }

    public String getCardToNumber() {
        return cardToNumber;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Amount getAmountAfterTax() {
        return amountAfterTax;
    }

    public Amount getPercentAmount() {
        return percentAmount;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setCardFromNumber(String cardFromNumber) {
        this.cardFromNumber = cardFromNumber;
    }

    public void setCardToNumber(String cardToNumber) {
        this.cardToNumber = cardToNumber;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return String.format("Данные транзакции: Id: %s. " +
                        "Номер карты отправителя: %s. " +
                        "Номер телефона отправителя: %s. " +
                        "Номер карты получателя: %s. " +
                        "Сумма перевода: %s. Сумма после уплаты комиссии: %s. Комиссия: %s",
                transactionId, cardFromNumber, phoneNumber, cardToNumber, amount, amountAfterTax, percentAmount);
    }
}
