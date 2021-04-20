package ru.netology.money_transfer_service.controllers;

import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.money_transfer_service.exceptions.*;
import ru.netology.money_transfer_service.exceptions.converter_exceptions.TransactionDataConverterException;
import ru.netology.money_transfer_service.models.ExceptionResponse;

import javax.validation.ValidationException;

@RestControllerAdvice
public class MoneyTransferControllerAdvice {
    private final int notFoundExceptionResponseId = 404;
    private final int internalServerErrorExceptionResponseId = 500;

    @ExceptionHandler(IncorrectSenderCardData.class)
    public ResponseEntity<ExceptionResponse> handleIncorrectSenderCardData(IncorrectSenderCardData e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), notFoundExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IncorrectVerificationCode.class)
    public ResponseEntity<ExceptionResponse> handleIncorrectVerificationCode(IncorrectVerificationCode e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), internalServerErrorExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ExceptionResponse> handleInsufficientFundsException(InsufficientFundsException e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), internalServerErrorExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TransactionCreationException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionCreationException(TransactionCreationException e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), internalServerErrorExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(WrongRecipientCardNumber.class)
    public ResponseEntity<ExceptionResponse> handleWrongRecipientCardNumber(WrongRecipientCardNumber e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), notFoundExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(СodeReadingException.class)
    public ResponseEntity<ExceptionResponse> handleСodeReadingException(СodeReadingException e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), notFoundExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TransactionDataConverterException.class)
    public ResponseEntity<ExceptionResponse> handleTransactionDataConverterException(TransactionDataConverterException e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), internalServerErrorExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(ValidationException e) {
        final var exceptionResponse = new ExceptionResponse(e.getMessage(), internalServerErrorExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<ExceptionResponse> handleParseException(ParseException e) {
        final var exceptionResponse = new ExceptionResponse("Невалидный json", internalServerErrorExceptionResponseId);
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
