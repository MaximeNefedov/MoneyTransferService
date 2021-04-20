package ru.netology.money_transfer_service.repositories;

import org.springframework.stereotype.Repository;
import ru.netology.money_transfer_service.models.transactions.Transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TransactionsRepository {
    private final Map<String, Transaction> transactions;
    private final AtomicLong transactionCounter;
    private final String serviceIdentifier = "mtsOpId_";

    public Map<String, Transaction> getTransactions() {
        return transactions;
    }

    public TransactionsRepository() {
        transactions = new ConcurrentHashMap<>();
        transactionCounter = new AtomicLong(1);
    }

    public String getTransactionId() {
        return serviceIdentifier + transactionCounter.get();
    }

    public boolean confirmCode(String transactionId, String code) {
        return transactions.get(transactionId).getSecretCode().equals(code);
    }

    public Transaction getTransactionById(String transactionId) {
        return transactions.get(transactionId);
    }

    public void addTransaction(Transaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
        transactionCounter.incrementAndGet();
    }

    public void removeTransaction(String transactionId) {
        transactions.remove(transactionId);
    }

    public String getPhoneNumber(String transactionId) {
        return transactions.get(transactionId).getPhoneNumber();
    }
}
