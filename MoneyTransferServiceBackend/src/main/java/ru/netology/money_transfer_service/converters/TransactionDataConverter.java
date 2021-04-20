package ru.netology.money_transfer_service.converters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.netology.money_transfer_service.exceptions.converter_exceptions.TransactionDataConverterException;
import ru.netology.money_transfer_service.models.Currencies;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.transactions.TransactionData;
import ru.netology.money_transfer_service.models.transactions.TransactionDataBuilder;

import java.util.Calendar;

public class TransactionDataConverter {
    private static final int CARD_NUMBER_SIZE = 16;
    private static final int CVV_CODE_SIZE = 3;
    private static final String EXCEPTION_MESSAGE = "Ошибка обработки данных";
    private static final String AMOUNT_EXAMPLE = "\"amount\":{\"currency\":\"[Наименование валюты]\",\"value\":[Cумма для списания (строго цифры)]\"";

    public static TransactionData getTransactionData(String json) throws ParseException {
        final var parser = new JSONParser();
        final Object obj = parser.parse(json);

        final var jsonObject = (JSONObject) obj;
        checkExistOfMainTransactionParts(jsonObject);

        final var transactionDataBuilder = new TransactionDataBuilder();

        final var cardFromNumber = jsonObject.get("cardFromNumber").toString();
        cardDataExist(cardFromNumber);
        checkCardNumber(cardFromNumber);
        transactionDataBuilder.setCardFromNumber(cardFromNumber);

        final var cardToNumber = jsonObject.get("cardToNumber").toString();
        cardToNumberExist(cardToNumber);
        checkCardNumber(cardToNumber);
        transactionDataBuilder.setCardToNumber(cardToNumber);

        final var cardFromValidTill = jsonObject.get("cardFromValidTill").toString();
        cardDataExist(cardToNumber);
        checkValidTill(cardFromValidTill);
        transactionDataBuilder.setCardFromValidTill(cardFromValidTill);

        final var cardFromCVV = jsonObject.get("cardFromCVV").toString();
        cardDataExist(cardFromCVV);
        checkCVV(cardFromCVV);
        transactionDataBuilder.setCardFromCVV(cardFromCVV);

        final var amountObj = jsonObject.get("amount");
        checkAmount(amountObj);
        final var amountJson = (JSONObject) amountObj;
        final var currency = amountJson.get("currency").toString();
        checkCurrency(currency);
        final var value = amountJson.get("value").toString();
        final var validValue = handleValue(value);
        final var amount = getAmount(currency, validValue);
        transactionDataBuilder.setAmount(amount);
        return transactionDataBuilder.build();
    }

    private static void checkExistOfMainTransactionParts(JSONObject jsonObject) {
        if (!jsonObject.containsKey("cardFromNumber") ||
                !jsonObject.containsKey("cardToNumber") ||
                !jsonObject.containsKey("cardFromValidTill") ||
                !jsonObject.containsKey("amount") ||
                !jsonObject.containsKey("cardFromCVV")) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE);
        }

        final var amount = (JSONObject) jsonObject.get("amount");
        if (!amount.containsKey("currency") || !amount.containsKey("value")) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE);
        }
    }

    private static String handleValue(String value) {
        if (value == null) {
            throw new TransactionDataConverterException("Сумма перевода не указана");

        }
        if (!value.matches("\\d+"))
            throw new TransactionDataConverterException("Значение суммы перевода должно состоять из цифр");

        if (Long.parseLong(value) <= 0)
            throw new TransactionDataConverterException("Значение суммы перевода должна быть положительным");

        return convertToValidValue(value);
    }

    private static String convertToValidValue(String value) {
        var chars = value.toCharArray();
        var result = new char[chars.length + 1];
        var counter = 0;
        for (int i = 0; i < result.length; i++) {
            if (i >= chars.length - 2) {
                if (i == chars.length - 2) {
                    result[i] = '.';
                } else {
                    result[i] = chars[counter++];
                }
            } else {
                result[i] = chars[counter++];
            }
        }
        return new String(result);
    }

    private static void checkCurrency(String currency) {
        if (currency == null) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": валюта не указана");
        }
        final var currencies = Currencies.values();
        boolean isCurrencyCorrect = false;
        for (Currencies baseCurrency : currencies) {
            if (baseCurrency.getDescription().equals(currency)) {
                isCurrencyCorrect = true;
                break;
            }
        }
        if (!isCurrencyCorrect) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": указанная валюта " + currency + " не поддерживается");
        }
    }

    private static void checkAmount(Object amount) {
        if (amount == null) {
            throw new TransactionDataConverterException(
                    String.format("%s: некорректно объявлен Amount." +
                            "\nПример правильного оформленя: %s", EXCEPTION_MESSAGE, AMOUNT_EXAMPLE)
            );
        }
    }

    private static Amount getAmount(String currency, String value) {
        return new Amount(Currencies.valueOf(currency), value);
    }

    private static void checkCardNumber(String cardNumber) {
        if (cardNumber.length() != CARD_NUMBER_SIZE || !cardNumber.matches("\\d+")) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": номер карты должен состоять из " + CARD_NUMBER_SIZE + " цифр");
        }
    }

    private static void cardDataExist(String cardData) {
        if (cardData == null || cardData.isEmpty()) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": введены неверные данные карты отправителя");
        }
    }

    private static void cardToNumberExist(String numberTo) {
        if (numberTo == null || numberTo.isEmpty()) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": введен неверный номер карты получателя");
        }
    }

    private static void checkValidTill(String cardValidTill) {
        if (cardValidTill.matches("\\d{2}\\\\/\\d{2}]"))
            cardValidTill = cardValidTill.replaceAll("\\\\", "");

        if (!cardValidTill.matches("\\d{2}/\\d{2}")) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": cрок годности карты объявлен в неверном формате: " + cardValidTill);
        }
        final var cardFromValidTillParts = cardValidTill.split("/");
        final var month = Integer.parseInt(cardFromValidTillParts[0]);
        final var year = Integer.parseInt(cardFromValidTillParts[1]);
        final var currentYear = Calendar.getInstance().get(Calendar.YEAR) % 100;
        var currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentMonth++;

        if (year < currentYear) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": истек срок годности карты");
        }
        if (year == currentYear) {
            if (month < currentMonth) {
                throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": истек срок годности карты");
            }
        }
    }

    private static void checkCVV(String cardCVV) {
        if (!cardCVV.matches("\\d{3}")) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": CVV код должен состоять из " + CVV_CODE_SIZE + " цифр");
        }
    }
}
