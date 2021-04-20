package ru.netology.money_transfer_service.services;

import org.springframework.stereotype.Service;
import ru.netology.money_transfer_service.exceptions.IncorrectVerificationCode;
import ru.netology.money_transfer_service.logger.Logger;
import ru.netology.money_transfer_service.models.Currencies;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.transactions.*;
import ru.netology.money_transfer_service.models.transactions.transaction_handlers.TransactionHandler;
import ru.netology.money_transfer_service.repositories.CardsRepository;
import ru.netology.money_transfer_service.repositories.TransactionsRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
public class TransactionHandlerService {
    private final TransactionsRepository transactionsRepository;
    private final CardsRepository cardsRepository;
    private final TransactionHandler transactionHandler;

    public TransactionHandlerService(TransactionsRepository transactionsRepository, CardsRepository cardsRepository, TransactionHandler transactionHandler) {
        this.transactionsRepository = transactionsRepository;
        this.cardsRepository = cardsRepository;
        this.transactionHandler = transactionHandler;
    }

    public Transaction handleTransaction(TransactionData transactionData) {
        Logger.getLogger().log("Начинается обработка транзакции", true);

        transactionHandler.checkingSendersCardData(transactionData); // проверка валидности данных карты и баланса

        final var cardToNumber = transactionData.getCardToNumber(); // проверка валидности номера карты получателя
        transactionHandler.checkingCardToNumber(cardToNumber);

        Logger.getLogger().log("Генерирование id транзакции...", true);

        final var transactionId = transactionHandler.generateTransactionId();

        Logger.getLogger().log("Генерирование кода подтверждения...", true);

        final var secretCode = generateSecretCode();

        final var taxValues = new TaxValues();
        final var transactionDataAmount = transactionData.getAmount();
        taxValues.calculateTaxes(transactionDataAmount.getValue(), transactionDataAmount.getCurrency());
        final var valueAfterTax = taxValues.getAmountAfterTaxAsString();
        final var percentValue = taxValues.getPercentAmountAsString();

        final var transaction = new TransactionBuilder()
                .setTransactionId(transactionId)
                .setCardFromNumber(transactionData.getCardFromNumber())
                .setCardToNumber(cardToNumber).setAmount(transactionData.getAmount())
                .setPhoneNumber(transactionData.getPhoneNumber())
                .setSecretCode(secretCode)
                .setValueAfterTax(valueAfterTax)
                .setPercentValue(percentValue)
                .build();

        transactionsRepository.addTransaction(transaction);
        return transaction;
    }

    private static class TaxValues {
        private static final BigDecimal percent = new BigDecimal("0.01");
        private BigDecimal valueAfterTax;
        private BigDecimal percentValue;
        private Currencies currency;

        public void calculateTaxes(BigDecimal base, Currencies currency) {
            percentValue = base.multiply(percent).setScale(2, RoundingMode.HALF_UP);
            valueAfterTax = base.subtract(percentValue);
            this.currency = currency;
        }

        public Amount getAmountAfterTaxAsString() {
            return new Amount(currency, valueAfterTax.toString());
        }

        public Amount getPercentAmountAsString() {
            return new Amount(currency, percentValue.toString());
        }
    }

    private String generateSecretCode() {
        final var random = new Random();
        return String.format("%04d", random.nextInt(10_000));
    }


    public boolean confirmTransaction(String transactionId, String code) {
        if (!confirmCode(transactionId, code)){
            final var message = "Код подтверждения неверный";
            // удалить транзакцию из репозитория
            transactionsRepository.removeTransaction(transactionId);
            Logger.getLogger().log(message, true);
            throw new IncorrectVerificationCode(message);
        } else {
            Logger.getLogger().log("Код подтвеждения успешно принят...", true);
        }

        return executeTransaction(transactionId);
    }

    public String getPhoneNumberByTransactionId(String transactionId) {
        final var phoneNumber = transactionsRepository.getPhoneNumber(transactionId);
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return phoneNumber;
    }

    private boolean confirmCode(String transactionId, String code) {
        return transactionsRepository.confirmCode(transactionId, code);
    }

    private boolean executeTransaction(String transactionId) {
        Logger.getLogger().log("Перевод денежных средств...", true);

        final var currentTransaction = transactionsRepository.getTransactionById(transactionId);
        final var cardFromNumber = currentTransaction.getCardFromNumber();
        final var cardToNumber = currentTransaction.getCardToNumber();

        final var cardFrom = cardsRepository.getCardByCardNumber(cardFromNumber);
        final var cardTo = cardsRepository.getCardByCardNumber(cardToNumber);
        final var amountWithTax = currentTransaction.getAmountAfterTax();
        final var amountWithoutTax = currentTransaction.getAmount();

        final var newCashBalance = cardFrom.writeOffMoneyFromTheCard(amountWithoutTax);
        cardTo.topUpCardBalance(amountWithTax);
        currentTransaction.setCompletedStatus();

        Logger.getLogger().log("Транзакция подтвеждена, перевод выполнен!", true);
        Logger.getLogger().log("Списано: " + amountWithoutTax
                + ". Ваш текущий баланс: " + newCashBalance
                + ". Баланс карты получателя: " + cardTo.getAmount(), true);

        return currentTransaction.isCompleted();
    }
}
