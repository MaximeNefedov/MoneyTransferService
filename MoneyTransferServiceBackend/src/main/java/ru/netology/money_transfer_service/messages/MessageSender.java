package ru.netology.money_transfer_service.messages;

import ru.netology.money_transfer_service.logger.Logger;

import java.io.*;

public class MessageSender extends Thread {
    private final String phoneNumber;
    private final String code;
    private final int sessionTime;
    private final String pattern = ".txt";
    private final MessageSession lock;

    public MessageSender(String phoneNumber, String code, MessageSession lock, int sessionTime) {
        this.phoneNumber = phoneNumber;
        this.code = code;
        this.sessionTime = sessionTime;
        this.lock = lock;
    }

    @Override
    public void run() {
        final var file = new File(phoneNumber + pattern);
        try {
            synchronized (lock.getWriteLock()) {
                try (final var out = new BufferedOutputStream(new FileOutputStream(file))) {
                    out.write(code.getBytes());
                    out.flush();
                    Logger.getLogger().log("Сообщение отправлено на телефон " +
                            phoneNumber + ", оно будет активно в течение " + (sessionTime / 1000.0) +  " секунд", true);
                    // Файл удалится спустя укзанное время
                    // (по аналогии с тем, как СМС - уведомления перестают быть действительными)
                    lock.setWrote(true);
                    lock.getWriteLock().notify();
                }
            }

            synchronized (lock.getReadLock()) {
                lock.getReadLock().wait(sessionTime);
                file.delete();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
