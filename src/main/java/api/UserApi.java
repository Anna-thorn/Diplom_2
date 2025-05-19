package api;

import io.qameta.allure.*;
import io.restassured.response.Response;
import java.util.Map;
import static org.apache.http.HttpStatus.*;

import models.User;

/**
 * Класс с методами API для пользователя
 */
public class UserApi extends BaseApi {
    private static final String REGISTER_ENDPOINT = "/api/auth/register";
    private static final String LOGIN_ENDPOINT = "/api/auth/login";
    private static final String USER_ENDPOINT = "/api/auth/user";
    private static final String LOGOUT_ENDPOINT = "/api/auth/logout";
    private static final String REFRESH_TOKEN_ENDPOINT = "/api/auth/token";

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return getBaseRequestSpec()
                .body(user)
                .when()
                .post(REGISTER_ENDPOINT);
    }

    @Step("Авторизация пользователя")
    public Response loginUser(User user) {
        return getBaseRequestSpec()
                .body(user)
                .when()
                .post(LOGIN_ENDPOINT);
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
        return getBaseRequestSpec()
                .body(Map.of("token", refreshToken))
                .when()
                .post(REFRESH_TOKEN_ENDPOINT);
    }

    @Step("Получение данных пользователя")
    public Response getUserData(String accessToken) {
        return getBaseRequestSpec()
                .header("Authorization", accessToken)
                .when()
                .get(USER_ENDPOINT);
    }

    @Step("Обновление данных пользователя")
    public Response updateUserData(String accessToken, User user) {
        return getBaseRequestSpec()
                .header("Authorization", accessToken)
                .body(user)
                .when()
                .patch(USER_ENDPOINT);
    }

    @Step("Выход из системы")
    public Response logoutUser(String refreshToken) {
        return getBaseRequestSpec()
                .body(Map.of("token", refreshToken))
                .when()
                .post(LOGOUT_ENDPOINT);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String accessToken) {
        return getBaseRequestSpec()
                .header("Authorization", accessToken)
                .when()
                .delete(USER_ENDPOINT);  // DELETE /api/auth/user
    }
}