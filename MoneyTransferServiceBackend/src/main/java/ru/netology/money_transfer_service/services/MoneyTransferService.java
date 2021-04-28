package ru.netology.money_transfer_service.services;

import org.springframework.stereotype.Service;
import ru.netology.money_transfer_service.logger.Logger;
import ru.netology.money_transfer_service.models.ConfirmationData;
import ru.netology.money_transfer_service.models.OperationId;
import ru.netology.money_transfer_service.models.transactions.TransactionData;

@Service
public class MoneyTransferService {
    private final TransactionHandlerService transactionHandlerService;
    private final MessageService messageService;
    private static final Logger LOGGER = Logger.getLogger();

    public MoneyTransferService(TransactionHandlerService transactionHandlerService, MessageService messageService) {
        this.transactionHandlerService = transactionHandlerService;
        this.messageService = messageService;
    }

    public OperationId processTransaction(TransactionData transactionData) {
        final var transaction = transactionHandlerService.handleTransaction(transactionData);
        LOGGER.log(transaction.toString(), true);
        messageService.sendMessage(transaction);
        return new OperationId(transaction.getTransactionId());
    }

    public OperationId confirmTransaction(ConfirmationData confirmationData) {
        final var operationId = confirmationData.getOperationId();
        final var phoneNumber = transactionHandlerService.getPhoneNumberByTransactionId(operationId);
        final var code = messageService.readMessage(operationId, phoneNumber);
        transactionHandlerService.confirmTransaction(operationId, code);
        return new OperationId(confirmationData.getOperationId());
    }
}
