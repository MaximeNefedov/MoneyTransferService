package ru.netology.money_transfer_service.models.transactions.transaction_handlers;

import org.springframework.stereotype.Component;
import ru.netology.money_transfer_service.exceptions.IncorrectSenderCardData;
import ru.netology.money_transfer_service.exceptions.InsufficientFundsException;
import ru.netology.money_transfer_service.exceptions.WrongRecipientCardNumber;
import ru.netology.money_transfer_service.logger.Logger;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.cards.Card;
import ru.netology.money_transfer_service.models.transactions.TransactionData;
import ru.netology.money_transfer_service.repositories.CardsRepository;
import ru.netology.money_transfer_service.repositories.TransactionsRepository;

@Component
public class DefaultTransactionHandler implements TransactionHandler {
    private final CardsRepository cardsRepository;
    private final TransactionsRepository transactionsRepository;
    private static final Logger LOGGER = Logger.getLogger();

    public DefaultTransactionHandler(CardsRepository cardsRepository, TransactionsRepository transactionsRepository) {
        this.cardsRepository = cardsRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Override
    public String generateTransactionId() {
        return transactionsRepository.getTransactionId();
    }

    @Override
    public boolean checkingSendersCardData(TransactionData transactionData) {
        LOGGER.log("Обработка данных карты отправителя...", true);

        final var cardFromNumber = transactionData.getCardFromNumber();
        final var cardFromRepository = cardsRepository.getCardByCardNumber(cardFromNumber);

        if (cardFromRepository == null) {
            final var message = "Введен неверный номер, карта не найдена";
            LOGGER.log(message, true);
            throw new IncorrectSenderCardData(message);
        }

        final var cardFromCVV = transactionData.getCardFromCVV() ;
        final var cardFromValidTill = transactionData.getCardFromValidTill();

        if (!checkCardData(cardFromRepository, cardFromCVV, cardFromValidTill.toString())) {
            final var message = "Введены неверные данные карты";
            LOGGER.log(message, true);
            throw new IncorrectSenderCardData(message);
        }

        final var amount = transactionData.getAmount();
        if (!checkMoneyBalance(cardFromRepository, amount)) {
            final var message = "Недостаточно средств для списания";
            LOGGER.log(message, true);
            throw new InsufficientFundsException(message);
        }

        LOGGER.log("Данные карты отправителя верны!", true);

        transactionData.setPhoneNumber(cardFromRepository.getPhoneNumber());
        return true;
    }

    private boolean checkCardData(Card cardFromRepository, String cardFromCVV, String cardFromValidTill) {
        return cardFromRepository.getCardFromCVV().equals(cardFromCVV)
                && cardFromRepository.getCardFromValidTill().equals(cardFromValidTill);
    }

    private boolean checkMoneyBalance(Card card, Amount amount) {
        final var cardBalanceValue = card.getAmount().getValue();
        final var valueForTransaction = amount.getValue();
        return cardBalanceValue.compareTo(valueForTransaction) >= 0;
    }

    @Override
    public boolean checkingCardToNumber(String numberToCard) {
        LOGGER.log("Проверка номера карты получателя...", true);
        if (cardsRepository.getCardByCardNumber(numberToCard) == null) {
            final var message = "Введен неверный номер карты получателя...";
            LOGGER.log(message, true);
            throw new WrongRecipientCardNumber(message);
        }
        return true;
    }
}
