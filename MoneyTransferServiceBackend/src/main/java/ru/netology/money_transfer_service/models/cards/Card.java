package ru.netology.money_transfer_service.models.cards;

import java.math.BigDecimal;
import java.util.Objects;

public class Card {
    private final String number;
    private final String cardFromValidTill;
    private final String cardFromCVV;
    private final Amount amount;
    private String phoneNumber;

    public Card(String number, String cardFromValidTill, String cardFromCVV, Amount amount, String phoneNumber) {
        this.number = number;
        this.cardFromValidTill = cardFromValidTill;
        this.cardFromCVV = cardFromCVV;
        this.amount = amount;
        this.phoneNumber = phoneNumber;
    }

    public Card(String number, String cardFromValidTill, String cardFromCVV, Amount amount) {
        this.number = number;
        this.cardFromValidTill = cardFromValidTill;
        this.cardFromCVV = cardFromCVV;
        this.amount = amount;
    }

    public void topUpCardBalance(Amount amountForTransaction) {
        final var result = amount.getValue().add(amountForTransaction.getValue());
        amount.setValue(result);
    }

    public Amount writeOffMoneyFromTheCard(Amount amountForTransaction) {
        final var result = amount.getValue().subtract(amountForTransaction.getValue());
        amount.setValue(result);
        return amount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNumber() {
        return number;
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

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Card card = (Card) o;
//        return number.equals(card.number) && cardFromValidTill.equals(card.cardFromValidTill) && cardFromCVV.equals(card.cardFromCVV);
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(number, card.number) && Objects.equals(cardFromValidTill, card.cardFromValidTill) && Objects.equals(cardFromCVV, card.cardFromCVV);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, cardFromValidTill, cardFromCVV);
    }

    @Override
    public String toString() {
        return "Card{" +
                "number='" + number + '\'' +
                ", cardFromValidTill='" + cardFromValidTill + '\'' +
                ", cardFromCVV='" + cardFromCVV + '\'' +
                ", amount=" + amount +
                '}';
    }
}
