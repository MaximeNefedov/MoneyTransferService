package ru.netology.money_transfer_service.repositories;

import org.springframework.stereotype.Repository;
import ru.netology.money_transfer_service.models.transactions.Transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TransactionsRepository {
    private final Map<String, Transaction> transactions;
    private static int transactionCounter = 0;
    private static final String SERVICE_IDENTIFIER = "mtsOpId_";

    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public TransactionsRepository() {
        transactions = new ConcurrentHashMap<>();
    }

    public synchronized String getTransactionId() {
        return SERVICE_IDENTIFIER + transactionCounter;
    }

    public boolean confirmCode(String transactionId, String code) {
        return transactions.get(transactionId).getSecretCode().equals(code);
    }

    public Transaction getTransactionById(String transactionId) {
        return transactions.get(transactionId);
    }

    public synchronized void addTransaction(Transaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
        transactionCounter++;
    }

    public void removeTransaction(String transactionId) {
        transactions.remove(transactionId);
    }

    public String getPhoneNumber(String transactionId) {
        return transactions.get(transactionId).getPhoneNumber();
    }
}
