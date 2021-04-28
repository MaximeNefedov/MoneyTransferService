package ru.netology.money_transfer_service.models.transactions;

import ru.netology.money_transfer_service.models.cards.Amount;

import java.util.Objects;

public class TransactionData {
    private String cardFromNumber;
    private String cardToNumber;
    private String cardFromValidTill;
    private String cardFromCVV;
    private Amount amount;
    private String phoneNumber;

    public boolean allFieldsArePresent() {
        return cardFromNumber != null &&
                cardToNumber != null &&
                cardFromValidTill != null &&
                cardFromCVV != null &&
                amount != null &&
                phoneNumber != null;
    }

    public void setCardFromNumber(String cardFromNumber) {
        this.cardFromNumber = cardFromNumber;
    }

    public void setCardToNumber(String cardToNumber) {
        this.cardToNumber = cardToNumber;
    }

    public void setCardFromValidTill(String cardFromValidTill) {
        this.cardFromValidTill = cardFromValidTill;
    }

    public void setCardFromCVV(String cardFromCVV) {
        this.cardFromCVV = cardFromCVV;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public String getCardFromNumber() {
        return cardFromNumber;
    }

    public String getCardFromValidTill() {
        return cardFromValidTill;
    }

    public String getCardFromCVV() {
        return cardFromCVV;
    }

    public Amount getAmount() {
        return amount;
    }

    public String getCardToNumber() {
        return cardToNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "TransactionData{" +
                "number='" + cardFromNumber + '\'' +
                ", cardFromValidTill='" + cardFromValidTill + '\'' +
                ", cardFromCVV='" + cardFromCVV + '\'' +
                ", amount=" + amount +
                ", cardToNumber='" + cardToNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionData that = (TransactionData) o;
        return Objects.equals(cardFromNumber, that.cardFromNumber) && Objects.equals(cardFromValidTill, that.cardFromValidTill) && Objects.equals(cardFromCVV, that.cardFromCVV) && Objects.equals(amount, that.amount) && Objects.equals(cardToNumber, that.cardToNumber) && Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardFromNumber, cardFromValidTill, cardFromCVV, amount, cardToNumber, phoneNumber);
    }
}
