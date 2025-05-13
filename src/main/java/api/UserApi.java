package api;

import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

import models.User;

/**
 * Класс с методами API для пользователя
 */
public class UserApi {
    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site";
    private static final String REGISTER = "/api/auth/register";
    private static final String LOGIN = "/api/auth/login";
    private static final String USER = "/api/auth/user";
    private static final String LOGOUT = "/api/auth/logout";
    private static final String REFRESH_TOKEN_ENDPOINT = "/api/auth/token";

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(user)
                .when()
                .post(REGISTER);
    }

    @Step("Авторизация пользователя")
    public Response loginUser(User user) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(user)
                .when()
                .post(LOGIN);
    }

    @Step("Получение accessToken")
    /* токен используется в запросах к эндпоинту auth/user,
    чтобы получить, обновить и удалить данные пользователя.
    Срок жизни токена — 20 минут */
    public String getAccessToken(User user) {
        return loginUser(user)
                .then()
                .statusCode(SC_OK)
                .extract()
                .path("accessToken");
    }

    @Step("Обновление токена")
    /* refreshToken используется для выхода из системы и для получения нового accessToken,
     если последний перестал подходить и просрочился */
    public Response refreshToken(String refreshToken) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(Map.of("token", refreshToken))
                .when()
                .post(REFRESH_TOKEN_ENDPOINT);
    }

    @Step("Получение данных пользователя")
    public Response getUserData(String accessToken) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .get(USER);
    }

    @Step("Обновление данных пользователя")
    public Response updateUserData(String accessToken, User user) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .contentType("application/json")
                .body(user)
                .when()
                .patch(USER);
    }

    @Step("Выход из системы")
    public Response logoutUser(String refreshToken) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(Map.of("token", refreshToken))
                .when()
                .post(LOGOUT);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String accessToken) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .header("Authorization", accessToken)
                .when()
                .delete(USER);  // DELETE /api/auth/user
    }
}