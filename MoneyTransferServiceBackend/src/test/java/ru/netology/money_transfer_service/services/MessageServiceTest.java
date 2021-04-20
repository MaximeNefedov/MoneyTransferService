package ru.netology.money_transfer_service.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.netology.money_transfer_service.exceptions.СodeReadingException;
import ru.netology.money_transfer_service.messages.MessageReader;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.transactions.Transaction;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class MessageServiceTest {
    @Autowired
    private MessageService messageService;
    private Transaction transaction;

    @BeforeEach
    private void createTransaction() {
        final var transactionId = "id_1";
        final var phoneNumber = "88005553535";
        final var code = "9999";
        transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setPhoneNumber(phoneNumber);
        transaction.setSecretCode(code);
    }

    @Test
    void sendAndReadMessageToConfirmCode() {
        messageService.sendMessage(transaction);

        final var readCode = messageService.readMessage(transaction.getTransactionId(), transaction.getPhoneNumber());
        assertEquals(transaction.getSecretCode(), readCode);
    }

    @Test
    void readMessageMethodShouldThrowСodeReadingException() {
        transaction.setSecretCode("");
        messageService.sendMessage(transaction);
        assertThrows(СodeReadingException.class,
                () -> messageService.readMessage(transaction.getTransactionId(), transaction.getPhoneNumber()));
    }
}