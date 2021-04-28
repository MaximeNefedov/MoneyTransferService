package ru.netology.money_transfer_service.intagration_tests;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
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
    private static final String SEPARATOR = File.separator;
    private static final String RESOURCE_DIR = "jsons_for_integration_tests";
    private static final String PATH = SEPARATOR + RESOURCE_DIR + SEPARATOR;
    private static final String HOST = "http://localhost:";
    private static final String TRANSFER_ENDPOINT = "/transfer";
    private static final int PORT = 8080;

    @Container
    public static GenericContainer<?> backendApp = new GenericContainer<>("money_transfer_service_app:1.0")
            .withExposedPorts(PORT);


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
        final var validJson = readJson(PATH + "valid_json.json");
        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(validJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var statusCode = responseEntity.getStatusCode();
        final var responseEntityBody = responseEntity.getBody();
        final var validJsonFromResponse = "{\"operationId\":\"mtsOpId_1\"}";
        Assertions.assertAll(() -> Assertions.assertEquals(200, statusCode.value()),
                () -> Assertions.assertEquals(validJsonFromResponse, responseEntityBody));
    }

    @Test
    public void testingBadCardToNumberJsonRequest() {
        final var badCardToNumberJson = readJson(PATH + "bad_card_to_number.json");
        final String badCardDataResponseJson = "{\"message\":\"Ошибка обработки данных: номер карты должен состоять из 16 цифр\",\"id\":500}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardToNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();

        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(badCardDataResponseJson, responseEntityBody));
    }

    @Test
    public void testingBadCardFromNumberJsonRequest() {
        final var badCardFromNumberJson = readJson(PATH + "bad_card_from_number.json");
        final String badCardDataResponseJson = "{\"message\":\"Ошибка обработки данных: номер карты должен состоять из 16 цифр\",\"id\":500}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();

        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(badCardDataResponseJson, responseEntityBody));
    }

    @Test
    public void testingBadCardFromCvvJsonRequest() {
        final var badCardFromCvvJson = readJson(PATH + "bad_card_from_cvv.json");
        final String badCardFromCvvResponseJson = "{\"message\":\"Ошибка обработки данных: CVV код должен состоять из 3 цифр\",\"id\":500}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromCvvJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(badCardFromCvvResponseJson, responseEntityBody));
    }

    @Test
    public void testingBadCardFromCardValidTillJsonRequest() {
        final var badCardFromCardValidTillJson = readJson(PATH + "bad_card_from_card_valid_till.json");
        final String badCardFromCardValidTillResponseJson = "{\"message\":\"Ошибка обработки данных: истек срок годности карты\",\"id\":500}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromCardValidTillJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(badCardFromCardValidTillResponseJson, responseEntityBody));
    }

    @Test
    public void testingBadCardFromAmountValueJsonRequest() {
        final var badCardFromAmountValueJson = readJson(PATH + "bad_card_from_amount_value.json");
        final String badCardFromAmountValueResponseJson = "{\"message\":\"Значение суммы перевода должна быть положительным\",\"id\":500}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromAmountValueJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(badCardFromAmountValueResponseJson, responseEntityBody));
    }

    @Test
    public void testingBadCardFromAmountRurJsonRequest() {
        final var badCardFromAmountRurJson = readJson(PATH + "bad_card_from_amount_rur.json");
        final String badCardFromAmountRurResponseJson = "{\"message\":\"Ошибка обработки данных: указанная валюта DOLLAR не поддерживается\",\"id\":500}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(badCardFromAmountRurJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(badCardFromAmountRurResponseJson, responseEntityBody));
    }

    @Test
    public void testingEmptyJsonRequest() {
        final String badEmptyResponseJson = "{\"message\":\"Невалидный json\",\"id\":500}";

        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, null, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(badEmptyResponseJson, responseEntityBody));
    }

    @Test
    public void testingWrongCardFromNumberJsonRequest() {
        final var wrongCardFromNumberJson = readJson(PATH + "wrong_card_from_number.json");
        final String wrongCardFromNumberResponseJson = "{\"message\":\"Введен неверный номер, карта не найдена\",\"id\":404}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(wrongCardFromNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(404, entityStatusCode.value()),
                () -> Assertions.assertEquals(wrongCardFromNumberResponseJson, responseEntityBody));
    }

    @Test
    public void testingWrongCardFromDataJsonRequest() {
        final var wrongCardFromDataJson = readJson(PATH + "wrong_card_from_data.json");
        final String wrongCardFromDataResponseJson = "{\"message\":\"Введены неверные данные карты\",\"id\":404}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(wrongCardFromDataJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(404, entityStatusCode.value()),
                () -> Assertions.assertEquals(wrongCardFromDataResponseJson, responseEntityBody));
    }

    @Test
    public void testingWrongRecipientCardNumberJsonRequest() {
        final var wrongRecipientCardNumberJson = readJson(PATH + "wrong_recipient_card_number.json");
        final String wrongRecipientCardNumberJsonResponseJson = "{\"message\":\"Введен неверный номер карты получателя...\",\"id\":404}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(wrongRecipientCardNumberJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(404, entityStatusCode.value()),
                () -> Assertions.assertEquals(wrongRecipientCardNumberJsonResponseJson, responseEntityBody));
    }

    @Test
    public void testingInsufficientFundsToWriteOffJsonJsonRequest() {
        final var insufficientFundsToWriteOffJson = readJson(PATH + "insufficient_funds_to_write_off.json");
        final var insufficientFundsToWriteOffResponseJson = "{\"message\":\"Недостаточно средств для списания\",\"id\":500}";

        final var httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        HttpEntity<String> httpEntity = new HttpEntity<>(insufficientFundsToWriteOffJson, httpHeaders);
        ResponseEntity<String> responseEntity = testRestTemplate
                .postForEntity(HOST + backendApp.getMappedPort(PORT) + TRANSFER_ENDPOINT, httpEntity, String.class);

        final var responseEntityBody = responseEntity.getBody();
        final var entityStatusCode = responseEntity.getStatusCode();
        Assertions.assertAll(() -> Assertions.assertEquals(500, entityStatusCode.value()),
                () -> Assertions.assertEquals(insufficientFundsToWriteOffResponseJson, responseEntityBody));
    }
}
