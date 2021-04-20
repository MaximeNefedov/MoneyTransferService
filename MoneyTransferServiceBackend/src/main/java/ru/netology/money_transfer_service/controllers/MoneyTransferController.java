package ru.netology.money_transfer_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.netology.money_transfer_service.models.ConfirmationData;
import ru.netology.money_transfer_service.models.OperationId;
import ru.netology.money_transfer_service.models.transactions.TransactionData;
import ru.netology.money_transfer_service.services.MoneyTransferService;

@CrossOrigin
@RestController
@RequestMapping
public class MoneyTransferController {

    private final MoneyTransferService service;

    public MoneyTransferController(MoneyTransferService service) {
        this.service = service;
    }

    @PostMapping("/transfer")
    public ResponseEntity<OperationId> transfer(TransactionData data) {
        final var operationId = service.processTransaction(data);
        return new ResponseEntity<>(operationId, HttpStatus.OK);
    }

    @PostMapping("/confirmOperation")
    public ResponseEntity<OperationId> confirm(@RequestBody ConfirmationData confirmationData) {
        final var operationId = service.confirmTransaction(confirmationData);
        return new ResponseEntity<>(operationId, HttpStatus.OK);
    }
}
