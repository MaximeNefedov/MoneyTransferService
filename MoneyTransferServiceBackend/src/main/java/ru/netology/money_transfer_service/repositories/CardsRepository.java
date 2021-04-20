package ru.netology.money_transfer_service.repositories;

import org.springframework.stereotype.Repository;
import ru.netology.money_transfer_service.models.Currencies;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.cards.Card;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CardsRepository {
    private final Map<String, Card> cashAccounts;

    public CardsRepository() {
        cashAccounts = new ConcurrentHashMap<>();

        // user 1
        final var amount = new Amount(Currencies.RUR, "150000.00");
        final var card = new Card(" 8888777766665555", "11/22", "331", amount, "89313214873");
        cashAccounts.put("8888777766665555", card);

        // user 2
        final var amount2 = new Amount(Currencies.RUR, "550000.99");
        final var card2 = new Card("1111222233334444", "05/25", "112", amount2, "88005553535");
        cashAccounts.put("1111222233334444", card2);
    }

    public Card getCardByCardNumber(String cardNumber) {
        return cashAccounts.get(cardNumber);
    }
}
