package ru.netology.money_transfer_service.intagration_tests;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MoneyTransferServiceTest {
    private static final String HOST = "http://localhost:";
    private static final String TRANSFER_ENDPOINT = "/transfer";
    private static final String RESOURCE_DIR = "jsons_for_integration_tests";
    private static final String SEPARATOR = File.separator;
    private static final int PORT = 8080;
    private static final int VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE = 500;
    private static final int VALID_NOT_FOUND_RESPONSE_STATUS_CODE = 404;
    private static final int VALID_OK_RESPONSE_STATUS_CODE = 200;
    private static final String BAD_CARD_DATA_RESPONSE_JSON = "{\"message\":\"Ошибка обработки данных: номер карты должен состоять из 16 цифр\",\"id\":500}";
    private static final String BAD_CARD_FROM_CVV_RESPONSE_JSON = "{\"message\":\"Ошибка обработки данных: CVV код должен состоять из 3 цифр\",\"id\":500}";
    private static final String BAD_CARD_FROM_CARD_VALID_TILL_RESPONSE_JSON = "{\"message\":\"Ошибка обработки данных: истек срок годности карты\",\"id\":500}";
    private static final String BAD_CARD_FROM_AMOUNT_VALUE_RESPONSE_JSON = "{\"message\":\"Значение суммы перевода должна быть положительным\",\"id\":500}";
    private static final String BAD_CARD_FROM_AMOUNT_RUR_RESPONSE_JSON = "{\"message\":\"Ошибка обработки данных: указанная валюта DOLLAR не поддерживается\",\"id\":500}";
    private static final String BAD_EMPTY_RESPONSE_JSON = "{\"message\":\"Невалидный json\",\"id\":500}";
    private static final String INSUFFICIENT_FUNDS_TO_WRITE_OFF_RESPONSE_JSON = "{\"message\":\"Недостаточно средств для списания\",\"id\":500}";

    private static final String WRONG_CARD_FROM_NUMBER_RESPONSE_JSON = "{\"message\":\"Введен неверный номер, карта не найдена\",\"id\":404}";
    private static final String WRONG_CARD_FROM_DATA_RESPONSE_JSON = "{\"message\":\"Введены неверные данные карты\",\"id\":404}";
    private static final String WRONG_RECIPIENT_CARD_NUMBER_JSON_RESPONSE_JSON = "{\"message\":\"Введен неверный номер карты получателя...\",\"id\":404}";


    private static String validJson;
    private static final String VALID_JSON_FILENAME = "valid_json.json";

    private static String badCardToNumberJson;
    private static final String BAD_CARD_TO_NUMBER_JSON_FILENAME = "bad_card_to_number.json";

    private static String badCardFromNumberJson;
    private static final String BAD_CARD_FROM_NUMBER_JSON_FILENAME = "bad_card_from_number.json";

    private static String badCardFromCvvJson;
    private static final String BAD_CARD_FROM_CVV_JSON_FILENAME = "bad_card_from_cvv.json";

    private static String badCardFromCardValidTillJson;
    private static final String BAD_CARD_FROM_CARD_VALID_TILL_JSON_FILENAME = "bad_card_from_card_valid_till.json";

    private static String badCardFromAmountValueJson;
    private static final String BAD_CARD_FROM_AMOUNT_VALUE_JSON_FILENAME = "bad_card_from_amount_value.json";

    private static String badCardFromAmountRurJson;
    private static final String BAD_CARD_FROM_AMOUNT_RUR_JSON_FILENAME = "bad_card_from_amount_rur.json";

    private static String wrongCardFromNumberJson;
    private static final String WRONG_CARD_FROM_NUMBER_JSON_FILENAME = "wrong_card_from_number.json";

    private static String wrongCardFromDataJson;
    private static final String WRONG_CARD_FROM_DATA_JSON_FILENAME = "wrong_card_from_data.json";

    private static String insufficientFundsToWriteOffJson;
    private static final String INSUFFICIENT_FUNDS_TO_WRITE_OFF_JSON_FILENAME = "insufficient_funds_to_write_off.json";

    private static String wrongRecipientCardNumberJson;
    private static final String WRONG_RECIPIENT_CARD_NUMBER_JSON_FILENAME = "wrong_recipient_card_number.json";

    @Container
    public static GenericContainer<?> backendApp = new GenericContainer<>("money_transfer_service_app:1.0")
            .withExposedPorts(PORT);

    @BeforeAll
    public static void setJsonsForTestFromResources() {
        final var partOfPath = SEPARATOR + RESOURCE_DIR + SEPARATOR;
        validJson = readJson(partOfPath + VALID_JSON_FILENAME);
        badCardToNumberJson = readJson(partOfPath + BAD_CARD_TO_NUMBER_JSON_FILENAME);
        badCardFromNumberJson = readJson(partOfPath + BAD_CARD_FROM_NUMBER_JSON_FILENAME);
        badCardFromCvvJson = readJson(partOfPath + BAD_CARD_FROM_CVV_JSON_FILENAME);
        badCardFromCardValidTillJson = readJson(partOfPath + BAD_CARD_FROM_CARD_VALID_TILL_JSON_FILENAME);
        badCardFromAmountValueJson = readJson(partOfPath + BAD_CARD_FROM_AMOUNT_VALUE_JSON_FILENAME);
        badCardFromAmountRurJson = readJson(partOfPath + BAD_CARD_FROM_AMOUNT_RUR_JSON_FILENAME);

        wrongCardFromNumberJson = readJson(partOfPath + WRONG_CARD_FROM_NUMBER_JSON_FILENAME);
        wrongCardFromDataJson = readJson(partOfPath + WRONG_CARD_FROM_DATA_JSON_FILENAME);
        insufficientFundsToWriteOffJson = readJson(partOfPath + INSUFFICIENT_FUNDS_TO_WRITE_OFF_JSON_FILENAME);
        wrongRecipientCardNumberJson = readJson(partOfPath + WRONG_RECIPIENT_CARD_NUMBER_JSON_FILENAME);
    }

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

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testingValidJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(validJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var statusCode = responseEntity.getStatusCode();
        final var responseEntityBody = responseEntity.getBody();
        final var validJsonFromResponse = "{\"operationId\":\"mtsOpId_1\"}";
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_OK_RESPONSE_STATUS_CODE, statusCode.value()),
                () -> Assertions.assertEquals(validJsonFromResponse, responseEntityBody));
    }

    @Test
    public void testingBadCardToNumberJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardToNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();

        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(BAD_CARD_DATA_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingBadCardFromNumberJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();

        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(BAD_CARD_DATA_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingBadCardFromCvvJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromCvvJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(BAD_CARD_FROM_CVV_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingBadCardFromCardValidTillJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromCardValidTillJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(BAD_CARD_FROM_CARD_VALID_TILL_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingBadCardFromAmountValueJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromAmountValueJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(BAD_CARD_FROM_AMOUNT_VALUE_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingBadCardFromAmountRurJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromAmountRurJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(BAD_CARD_FROM_AMOUNT_RUR_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingEmptyJsonRequest() {
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, null, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(BAD_EMPTY_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingWrongCardFromNumberJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(wrongCardFromNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_NOT_FOUND_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(WRONG_CARD_FROM_NUMBER_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingWrongCardFromDataJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(wrongCardFromDataJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_NOT_FOUND_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(WRONG_CARD_FROM_DATA_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingWrongRecipientCardNumberJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(wrongRecipientCardNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_NOT_FOUND_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(WRONG_RECIPIENT_CARD_NUMBER_JSON_RESPONSE_JSON, responseEntityBody));
    }

    @Test
    public void testingInsufficientFundsToWriteOffJsonJsonRequest() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(insufficientFundsToWriteOffJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(VALID_INTERNAL_SERVER_ERROR_RESPONSE_STATUS_CODE, entityStatusCode.value()),
                () -> Assertions.assertEquals(INSUFFICIENT_FUNDS_TO_WRITE_OFF_RESPONSE_JSON, responseEntityBody));
    }
}
