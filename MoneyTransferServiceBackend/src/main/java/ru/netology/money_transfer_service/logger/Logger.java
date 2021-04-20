package ru.netology.money_transfer_service.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {
    private static Logger instance = null;
    private static File file;
    private static int counter = 1;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SS");
    private static final Lock lock = new ReentrantLock();

    private Logger() {
        file = new File("moneyTransferLog.log");
    }

    public static Logger getLogger() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(String msg, boolean logToFile) {
        try {
            lock.lock();
            if (logToFile) {
                logToFile(msg);
            }
            logToConsole(msg);
            counter++;
        } finally {
            lock.unlock();
        }

    }
    private void logToFile(String msg) {
        try (final var out = new BufferedWriter(new FileWriter(file, true))) {
            final var date = new Date();
            out.write(simpleDateFormat.format(date) + " [" + counter + "] " + msg + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logToConsole(String msg) {
        final var date = new Date();
        System.out.println(simpleDateFormat.format(date) + " [" + counter + "] " + msg);
    }
}

