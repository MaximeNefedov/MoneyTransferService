package ru.netology.money_transfer_service.converters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.netology.money_transfer_service.exceptions.converter_exceptions.TransactionDataConverterException;
import ru.netology.money_transfer_service.models.Currencies;
import ru.netology.money_transfer_service.models.cards.Amount;
import ru.netology.money_transfer_service.models.transactions.TransactionDataBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionDataConverterTest {
    private static final String JSONS_FOR_UNIT_TESTS_DIRNAME = "jsons_for_unit_tests";
    private static final String SEPARATOR = File.separator;

    private static String readJson(String path) {
        final String jsonFromFile;
        try (final var is = new ClassPathResource(path).getInputStream();
             final var bufferedReader = new BufferedReader(new InputStreamReader(is))) {
            var jsonTmp = bufferedReader.lines().collect(Collectors.joining("\n"));
            var parser = new JSONParser();
            var parse = parser.parse(jsonTmp);
            var jsonObject = (JSONObject) parse;
            jsonFromFile = jsonObject.toJSONString();
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
        return jsonFromFile;
    }

    @Test
    void getTransferData() throws ParseException {
        final var jsonForTest = readJson(SEPARATOR + JSONS_FOR_UNIT_TESTS_DIRNAME + SEPARATOR + "valid_json.json");
        final var transferData = TransactionDataConverter.getTransactionData(jsonForTest);
        final var transferDataValid = new TransactionDataBuilder()
                .setCardFromNumber("8888777766665555")
                .setCardToNumber("1111222233334444")
                .setCardFromValidTill("11/22")
                .setCardFromCVV("331")
                .setAmount(new Amount(Currencies.RUR, "111122.00"))
                .build();
        assertEquals(transferData, transferDataValid);
    }

    @Test
    void getTransferDataMethodShouldThrowTransactionDataConverterException() {
        final var incorrectJsonForTest = readJson(SEPARATOR + JSONS_FOR_UNIT_TESTS_DIRNAME + SEPARATOR + "incorrect_json.json");
        assertThrows(TransactionDataConverterException.class,
                () -> TransactionDataConverter.getTransactionData(incorrectJsonForTest));
    }
}