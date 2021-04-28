package ru.netology.money_transfer_service.messages;

import ru.netology.money_transfer_service.logger.Logger;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MessageReader {
    private final String pattern = ".txt";
    private final MessageSession lock;
    private static final Logger LOGGER = Logger.getLogger();

    public MessageReader(MessageSession lock) {
        this.lock = lock;
    }

    public String readMessage(String phoneNumber) {
        String code = null;
        try {
            synchronized (lock.getWriteLock()) {
                while (!lock.isWrote()) {
                    lock.getWriteLock().wait();
                }
            }

            synchronized (lock.getReadLock()) {
                try (final var in = new BufferedInputStream(new FileInputStream(phoneNumber + pattern))) {
                    code = new String(in.readAllBytes());
                    LOGGER.log("Прочитан код " + code + " из СМС - уведомления", true);
                    lock.getReadLock().notify();
                }
            }
        } catch (IOException e) {
            final var message = "Ошибка доступа к файлу: " + phoneNumber + pattern;
            LOGGER.log(message, true);
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return code;
    }
}
