package ru.netology.money_transfer_service.services;

import org.springframework.stereotype.Service;
import ru.netology.money_transfer_service.exceptions.СodeReadingException;
import ru.netology.money_transfer_service.logger.Logger;
import ru.netology.money_transfer_service.messages.MessageReader;
import ru.netology.money_transfer_service.messages.MessageSender;
import ru.netology.money_transfer_service.messages.MessageSession;
import ru.netology.money_transfer_service.models.transactions.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MessageService {
    // По истечение времени ожидания подтверждения файл с кодом удалится из корневой директории
    private final int sessionTime = 500;
    private final Map<String, Map<String, MessageSession>> locks = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger();


    public void sendMessage(Transaction transaction) {
        final var phoneNumber = transaction.getPhoneNumber();
        final var secretCode = transaction.getSecretCode();
        final var transactionId = transaction.getTransactionId();

        final var lockForSession = createLockForSession(transactionId, phoneNumber);
        new MessageSender(phoneNumber, secretCode, lockForSession, sessionTime).start();
    }

    public String readMessage(String transactionId, String phoneNumber) {
        final var lockForSession = getLockForSession(transactionId, phoneNumber);
        final var messageReader = new MessageReader(lockForSession);
        final var code = messageReader.readMessage(phoneNumber);
        if (code == null || code.isEmpty()) {
            final var message = "Ошибка чтения секретного кода";
            LOGGER.log(message, true);
            throw new СodeReadingException(message);
        }
        return code;
    }

    private MessageSession createLockForSession(String transactionId, String phoneNumber) {
        final var session = new MessageSession(new Object(), new Object());
        Map<String, MessageSession> mapTmp = new HashMap<>(); // связка: номер телефона - лок
        mapTmp.put(phoneNumber, session); // связка: номер транзакции (унивальный идентификатор) - мапа (номер телефона - лок)
        locks.put(transactionId, mapTmp);
        return session;
    }

    private MessageSession getLockForSession(String transactionId, String phoneNumber) {
        Map<String, MessageSession> mapTmp = locks.get(transactionId);
        return mapTmp.get(phoneNumber);
    }
}
