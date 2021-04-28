package ru.netology.money_transfer_service.services;

import org.springframework.stereotype.Service;
import ru.netology.money_transfer_service.exceptions.IncorrectVerificationCode;
import ru.netology.money_transfer_service.logger.Logger;
import ru.netology.money_transfer_service.models.transactions.TaxCalculator;
import ru.netology.money_transfer_service.models.transactions.Transaction;
import ru.netology.money_transfer_service.models.transactions.TransactionBuilder;
import ru.netology.money_transfer_service.models.transactions.TransactionData;
import ru.netology.money_transfer_service.models.transactions.transaction_handlers.TransactionHandler;
import ru.netology.money_transfer_service.repositories.CardsRepository;
import ru.netology.money_transfer_service.repositories.TransactionsRepository;

import java.util.Random;

@Service
public class TransactionHandlerService {
    private final TransactionsRepository transactionsRepository;
    private final CardsRepository cardsRepository;
    private final TransactionHandler transactionHandler;
    private static final Logger LOGGER = Logger.getLogger();

    public TransactionHandlerService(TransactionsRepository transactionsRepository, CardsRepository cardsRepository, TransactionHandler transactionHandler) {
        this.transactionsRepository = transactionsRepository;
        this.cardsRepository = cardsRepository;
        this.transactionHandler = transactionHandler;
    }

    public Transaction handleTransaction(TransactionData transactionData) {
        LOGGER.log("Начинается обработка транзакции", true);

        transactionHandler.checkingSendersCardData(transactionData); // проверка валидности данных карты и баланса

        final var cardToNumber = transactionData.getCardToNumber(); // проверка валидности номера карты получателя
        transactionHandler.checkingCardToNumber(cardToNumber);

        LOGGER.log("Генерирование id транзакции...", true);

        final var transactionId = transactionHandler.generateTransactionId();

        LOGGER.log("Генерирование кода подтверждения...", true);

        final var secretCode = generateSecretCode();

        final var transactionDataAmount = transactionData.getAmount();
        final var taxValues = TaxCalculator.calculateTaxes(transactionDataAmount.getValue(), transactionDataAmount.getCurrency());
        final var valueAfterTax = taxValues.getAmountAfterTax();
        final var percentValue = taxValues.getPercentAmount();

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

    private String generateSecretCode() {
        final var random = new Random();
        return String.format("%04d", random.nextInt(10_000));
    }

    public boolean confirmTransaction(String transactionId, String code) {
        if (!confirmCode(transactionId, code)){
            final var message = "Код подтверждения неверный";
            // удалить транзакцию из репозитория
            transactionsRepository.removeTransaction(transactionId);
            LOGGER.log(message, true);
            throw new IncorrectVerificationCode(message);
        } else {
            LOGGER.log("Код подтвеждения успешно принят...", true);
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
        LOGGER.log("Перевод денежных средств...", true);

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

        LOGGER.log("Транзакция подтвеждена, перевод выполнен!", true);
        LOGGER.log("Списано: " + amountWithoutTax
                + ". Ваш текущий баланс: " + newCashBalance
                + ". Баланс карты получателя: " + cardTo.getAmount(), true);

        return currentTransaction.isCompleted();
    }
}
