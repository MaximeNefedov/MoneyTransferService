package ru.netology.money_transfer_service.converters;

import ru.netology.money_transfer_service.exceptions.converter_exceptions.TransactionDataConverterException;
import ru.netology.money_transfer_service.models.Currencies;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.transactions.TransactionData;

import java.util.*;
import java.util.regex.Pattern;

// Класс TransactionDataConverter осуществляет конвертирование входного json в соответствии с определенной спецификацией
// и производит проверку валидности входных значений:
// 1) Номер карты как отправителя, так и получателя должен состоять из 16 цифр, в противном случае будет выброшено
// TransactionDataConverterException с пояснением

// 2) Номер СVV должен состоять из 3 цифр
// 3) Срок годности карты должен быть оформлен в формате: [месяц (04)] / [год (21)].
// Конвертер также проверяет, истек ли срок годности карты
// 4) Пустые значения не допускаются

@Deprecated
public class TransactionDataConverterRaw {
    private static final int CARD_NUMBER_SIZE = 16;
    private static final int CVV_CODE_SIZE = 3;
    private static final String EXCEPTION_MESSAGE = "Ошибка обработки данных";
    private static final String AMOUNT_EXAMPLE = "\"amount\":{\"currency\":\"[Наименование валюты]\",\"value\":[Cумма для списания (строго цифры)]\"";

    public TransactionData getTransferData(String requestJson) {
        final var requestTmp = deleteBraces(requestJson);
        final var mainJsonParts = getMainJsonParts(requestTmp);
        final var mainJsonPartsWithoutQuotes = deleteQuotes(mainJsonParts);
        final var cardInfoAsString = mainJsonPartsWithoutQuotes[0];
        final var amountAsString = mainJsonPartsWithoutQuotes[1];
        final var amount = getAmount(amountAsString);

        final var cardInfoParts = getCardInfoParts(cardInfoAsString);

        final var cardToNumber = getCardToNumber(cardInfoParts);
        return getTransactionData(cardInfoParts, amount, cardToNumber);
    }

    private String deleteBraces(String requestJson) {
        return requestJson.replaceAll("[\\{\\}]", "");
    }

//    private void getMainJsonParts()

    private String[] getMainJsonParts(String requestTmp) {
        final var stringsTmp = new String[2];
        final var pattern = Pattern.compile("(.+),(\"amount.+)");
        final var matcher = pattern.matcher(requestTmp);
        while (matcher.find()) {
            stringsTmp[0] = matcher.group(1);
            stringsTmp[1] = matcher.group(2);
        }

        for (String part : stringsTmp) {
            System.out.println(part);
            if (part == null || part.isEmpty()) {
                throw new TransactionDataConverterException(EXCEPTION_MESSAGE);
            }
        }
        return stringsTmp;
    }

    private String[] deleteQuotes(String[] mainJsonParts) {
        String[] mainJsonPartsWithoutQuotes = new String[mainJsonParts.length];
        for (int i = 0; i < mainJsonParts.length; i++) {
            mainJsonPartsWithoutQuotes[i] = mainJsonParts[i].replaceAll("\"", "");
        }
        return mainJsonPartsWithoutQuotes;
    }

    private Amount getAmount(String amountAsString) {
        final var pattern = Pattern.compile("amount:(currency:\\D+,value:\\d+$)");
        final var matcher = pattern.matcher(amountAsString);
        String amountParams = null;
        while (matcher.find()) {
            amountParams = matcher.group(1);
        }
        if (amountParams != null) {
            String[] split = amountParams.split(",");
            final var currency = getCurrentAmountPart(split[0]);
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

            final var value = getCurrentAmountPart(split[1]);
            if (Integer.parseInt(value) <= 0) {
                throw new TransactionDataConverterException("Значение суммы перевода должна быть положительным");
            }

            final var validValue = convertToValidValue(value);

            return new Amount(Currencies.valueOf(currency), validValue);
        } else {
            throw new TransactionDataConverterException(
                    String.format("%s: некорректно объявлен Amount." +
                            "\nПример правильного оформленя: %s" +
                            "\nВведенное значение: %s", EXCEPTION_MESSAGE, AMOUNT_EXAMPLE, amountAsString)
            );
        }
    }

    private String convertToValidValue(String value) {
//        При попытке перевести 999 руб. фронт прислылает значение вида 99900

//            Если число 99900 представлять как 999
//            char[] result = new char[chars.length - 2];
//            for (int i = 0; i < chars.length - 2; i++) {
//                result[i] = chars[i];
//            }
//            String validValue = new String(result);

//            Если число 99900 представлять как 999.00
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

    private String getCurrentAmountPart(String amountPart) {
        return amountPart.split(":")[1];
    }

    private Map<String, String> getCardInfoParts(String cardsInfoAsString) {
        Map<String, String> cardInfoParts = new HashMap<>();
        final var parts = cardsInfoAsString.split(",");

        for (String part : parts) {
            var splitPart = part.split(":");
            if (splitPart.length < 2) {
                throw new TransactionDataConverterException(String.format("%s:" +
                        "введены неверные данные карты отправителя." +
                        "\nВведенные данные: %s", EXCEPTION_MESSAGE, cardsInfoAsString)
                );
            }
            cardInfoParts.put(splitPart[0], splitPart[1]);
        }

        if (cardInfoParts.isEmpty()) {
            throw new TransactionDataConverterException(String.format("%s: " +
                    "введены неверные данные карты отправителя." +
                    "\nВведенные данные: %s", EXCEPTION_MESSAGE, cardsInfoAsString)
            );
        }

        return cardInfoParts;
    }

    private String getCardToNumber(Map<String, String> cardInfoParts) {
        final var cardToNumber = cardInfoParts.get("cardToNumber");
        if (cardToNumber == null) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": введен неверный номер карты отправителя");
        }

        if (!checkCardNumber(cardToNumber)) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": номер карты должен состоять из " + CARD_NUMBER_SIZE + " цифр");
        }
        return cardToNumber;
    }

    private boolean checkCardNumber(String cardNumber) {
        return cardNumber.length() == CARD_NUMBER_SIZE || cardNumber.matches("\\d+");
    }

    private TransactionData getTransactionData(Map<String, String> cardInfoParts, Amount amount, String cardToNumber) {
        final var cardFromNumber = cardInfoParts.get("cardFromNumber");
        if (cardFromNumber == null || cardFromNumber.isEmpty()) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": введены неверные данные карты отправителя.");
        }

        if (!checkCardNumber(cardFromNumber)) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": номер карты должен состоять из " + CARD_NUMBER_SIZE + " цифр");
        }

        final var cardFromValidTill = cardInfoParts.get("cardFromValidTill");
        if (cardFromValidTill == null || cardFromValidTill.isEmpty()) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": введены неверные данные карты отправителя.");
        }

        checkValidTill(cardFromValidTill);

        final var cardFromCVV = cardInfoParts.get("cardFromCVV");
        if (cardFromCVV == null || cardFromCVV.isEmpty()) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": введены неверные данные карты отправителя.");
        }

        checkCVV(cardFromCVV);

//        return new TransactionData(cardFromNumber, cardFromValidTill, cardFromCVV, cardToNumber, amount);
        return null;
    }

    private void checkCVV(String cardCVV) {
        if (!cardCVV.matches("\\d{3}")) {
            throw new TransactionDataConverterException(EXCEPTION_MESSAGE + ": CVV код должен состоять из " + CVV_CODE_SIZE + " цифр");
        }
    }

    private void checkValidTill(String cardValidTill) {
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
}
