package api;

import io.qameta.allure.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.*;
import static io.restassured.RestAssured.given;

/**
 * Класс с методами API для заказов
 */
public class OrderApi {
    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site";
    private static final String ORDERS_ENDPOINT = "/api/orders";

    @Step("Создание заказа")
    public Response createOrder(String[] ingredients, String accessToken) {
        RequestSpecification request = given()
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON);

        if (accessToken != null && !accessToken.isEmpty()) {
            request.header("Authorization", accessToken);
        }
        List<String> ingredientsList = (ingredients == null) ? new ArrayList<>() : Arrays.asList(ingredients);
        return request
                .body(Map.of("ingredients", ingredientsList))
                .post(ORDERS_ENDPOINT);
    }

    @Step("Получение списка заказов пользователя")
    public Response getUserOrders(String accessToken) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .get(ORDERS_ENDPOINT);
    }

    @Step("Попытка получить заказы без авторизации")
    public Response getUserOrdersWithoutAuth() {
        return given()
                .baseUri(BASE_URL)
                .when()
                .get(ORDERS_ENDPOINT);
    }
}