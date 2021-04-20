package ru.netology.money_transfer_service.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import ru.netology.money_transfer_service.exceptions.*;
import ru.netology.money_transfer_service.models.Currencies;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.cards.Card;
import ru.netology.money_transfer_service.models.transactions.Transaction;
import ru.netology.money_transfer_service.models.transactions.TransactionDataBuilder;
import ru.netology.money_transfer_service.repositories.CardsRepository;
import ru.netology.money_transfer_service.repositories.TransactionsRepository;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class DefaultTransactionHandlerServiceTest {
    private static String cardFromNumber;
    private static String cardFromValidTill;
    private static String cardFromCVV;
    private static Amount amountForTransaction;
    private static Amount amountCardFrom;
    private static Amount amountCardTo;
    private static String cardToNumber;
    private static String phoneNumber;
    private static Card cardFrom;
    private static Card cardTo;

    @BeforeAll
    public static void fillCardData() {
        cardFromNumber = "8888777766665555";
        cardFromValidTill = "11/22";
        cardFromCVV = "331";
        amountCardFrom = new Amount(Currencies.RUR, "1000.00");
        amountCardTo = new Amount(Currencies.RUR, "500");
        amountForTransaction = new Amount(Currencies.RUR, "100");
        phoneNumber = "89313214873";
        cardToNumber = "1111222233334444";
        cardFrom = new Card(cardFromNumber, cardFromValidTill, cardFromCVV, amountCardFrom, phoneNumber);
        cardTo = new Card(null, null, null, amountCardTo, null);
    }

    @Autowired
    private TransactionHandlerService transactionHandlerService;

    @MockBean
    private TransactionsRepository transactionsRepository;

    @MockBean
    private CardsRepository cardsRepository;

    @Test
    void handleTransaction() {
        final var transactionData = new TransactionDataBuilder()
                .setCardFromNumber(cardFromNumber)
                .setCardToNumber(cardToNumber)
                .setAmount(amountForTransaction)
                .setCardFromCVV(cardFromCVV)
                .setCardFromValidTill(cardFromValidTill)
                .build();

        Mockito.when(cardsRepository.getCardByCardNumber(cardFromNumber))
                .thenReturn(cardFrom);

        Mockito.when(cardsRepository.getCardByCardNumber(cardToNumber))
                .thenReturn(cardTo);

        Mockito.when(transactionsRepository.getTransactionId())
                .thenReturn("mtsOpId_[some_number]");

        final var transaction = transactionHandlerService.handleTransaction(transactionData);
        assertNotNull(transaction.getTransactionId());
    }

    @Test
    void handleTransactionMethodShouldThrowIncorrectSenderCardDataExceptionBecauseWrongCardNumber() {
        final var transactionDataWithWrongCardNumber = new TransactionDataBuilder()
                .setCardFromNumber("1121222233334444")
                .setCardToNumber(cardToNumber)
                .setAmount(amountForTransaction)
                .setCardFromCVV(cardFromCVV)
                .setCardFromValidTill(cardFromValidTill)
                .build();

        Mockito.when(cardsRepository.getCardByCardNumber(cardFromNumber))
                .thenReturn(cardFrom);

        assertThrows(IncorrectSenderCardData.class,
                () -> transactionHandlerService.handleTransaction(transactionDataWithWrongCardNumber));
    }

    @Test
    void handleTransactionMethodShouldThrowIncorrectSenderCardDataExceptionBecauseWrongCardData() {
        final var transactionDataWithWrongCVV = new TransactionDataBuilder()
                .setCardFromNumber(cardFromNumber)
                .setCardToNumber(cardToNumber)
                .setAmount(amountForTransaction)
                .setCardFromCVV("333")
                .setCardFromValidTill(cardFromValidTill)
                .build();

        Mockito.when(cardsRepository.getCardByCardNumber(cardFromNumber))
                .thenReturn(cardFrom);

        assertThrows(IncorrectSenderCardData.class,
                () -> transactionHandlerService.handleTransaction(transactionDataWithWrongCVV));
    }

    @Test
    void handleTransactionShouldThrowInsufficientFundsException() {
        final var transactionDataWithWrongCardBalance = new TransactionDataBuilder()
                .setCardFromNumber(cardFromNumber)
                .setCardToNumber(cardToNumber)
                .setAmount(amountForTransaction)
                .setCardFromCVV(cardFromCVV)
                .setCardFromValidTill(cardFromValidTill)
                .build();

        final var cardWithWrongBalance = new Card(cardFromNumber, cardFromValidTill,
                cardFromCVV, new Amount(Currencies.RUR, "10"), phoneNumber);

        Mockito.when(cardsRepository.getCardByCardNumber(cardFromNumber))
                .thenReturn(cardWithWrongBalance);

        assertThrows(InsufficientFundsException.class,
                () -> transactionHandlerService.handleTransaction(transactionDataWithWrongCardBalance));
    }

    @Test
    void handleTransactionShouldThrowWrongRecipientCardNumber() {
        final var transactionData = new TransactionDataBuilder()
                .setCardFromNumber(cardFromNumber)
                .setCardToNumber(cardToNumber)
                .setAmount(amountForTransaction)
                .setCardFromCVV(cardFromCVV)
                .setCardFromValidTill(cardFromValidTill)
                .build();

        Mockito.when(cardsRepository.getCardByCardNumber(cardFromNumber))
                .thenReturn(cardFrom);

        Mockito.when(cardsRepository.getCardByCardNumber(cardToNumber))
                .thenReturn(null);

        assertThrows(WrongRecipientCardNumber.class,
                () -> transactionHandlerService.handleTransaction(transactionData));
    }

    @Test
    void handleTransactionShouldThrowTransactionDataConverterException() {
        final var transactionData = new TransactionDataBuilder()
                .setCardFromNumber(cardFromNumber)
                .setCardToNumber(cardToNumber)
                .setAmount(amountForTransaction)
                .setCardFromCVV(cardFromCVV)
                .setCardFromValidTill(cardFromValidTill)
                .build();

        Mockito.when(cardsRepository.getCardByCardNumber(cardFromNumber))
                .thenReturn(cardFrom);

        Mockito.when(cardsRepository.getCardByCardNumber(cardToNumber))
                .thenReturn(cardTo);

        final var mockedTransaction = Mockito.mock(Transaction.class);

        Mockito.when(mockedTransaction.allFieldsArePresent()).thenReturn(false);

        assertThrows(TransactionCreationException.class,
                () -> transactionHandlerService.handleTransaction(transactionData));

    }

    @Test
    void confirmTransaction() {
        final var transactionId = "mtsOpId_1";
        final var code = "9999";

        Mockito.when(transactionsRepository.confirmCode(transactionId, code)).thenReturn(true);

        final var transaction = new Transaction();
        transaction.setCardFromNumber(cardFromNumber);
        transaction.setCardToNumber(cardToNumber);
        transaction.setValueAfterTax(amountForTransaction);

        Mockito.when(transactionsRepository.getTransactionById(transactionId)).thenReturn(transaction);

        Mockito.when(cardsRepository.getCardByCardNumber(cardFromNumber)).thenReturn(cardFrom);

        Mockito.when(cardsRepository.getCardByCardNumber(cardToNumber)).thenReturn(cardTo);

        boolean operationStatus = transactionHandlerService.confirmTransaction(transactionId, code);

        assertTrue(operationStatus);
    }

    @Test
    void confirmTransactionShouldThrowIncorrectVerificationCodeException() {
        final var transactionId = "mtsOpId_1";
        final var code = "9999";

        Mockito.when(transactionsRepository.confirmCode(transactionId, code)).thenReturn(false);

        assertThrows(IncorrectVerificationCode.class,
                () -> transactionHandlerService.confirmTransaction(transactionId, code));
    }
}