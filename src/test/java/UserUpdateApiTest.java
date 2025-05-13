import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import api.UserApi;
import models.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.UUID;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

/**
 * Проверки обновления данных
 */
public class UserUpdateApiTest {
    private static final String AUTH_ERROR = "You should be authorised";
    private static final String EMAIL_ALREADY_EXISTS_MESSAGE = "User with such email already exists";

    private UserApi userApi;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
        user = User.getRandomUser();
        Response response = userApi.createUser(user);
        accessToken = response.jsonPath().getString("accessToken");
    }


    @Test
    @DisplayName("Обновление email с авторизацией")
    public void updateEmailWithAuth() {
        String newEmail = User.getRandomUser().getEmail();
        User updateData = new User(newEmail, null, null);
        Response response = userApi.updateUserData(accessToken, updateData);
        assertEquals("Неверный код ответа при обновлении email",
                SC_OK, response.statusCode());
        assertEquals("Email не обновился",
                newEmail, response.jsonPath().getString("user.email"));
    }

    @Test
    @DisplayName("Обновление пароля с авторизацией")
    public void updatePasswordWithAuth() {
        User randomUser = User.getRandomUser();
        String newPassword = randomUser.getPassword();
        User updateData = new User(null, newPassword, null);
        Response updateResponse = userApi.updateUserData(accessToken, updateData);
        assertEquals("Неверный код ответа при обновлении пароля",
                SC_OK, updateResponse.statusCode());
        User newCredentials = new User(user.getEmail(), newPassword, null); // новый пароль работает
        Response loginResponse = userApi.loginUser(newCredentials);
        assertEquals("Вход с новым паролем не удался",
                SC_OK, loginResponse.statusCode());
        Response failedLogin = userApi.loginUser(user); // старый пароль больше не работает
        assertEquals("Вход со старым паролем должен возвращать 401",
                SC_UNAUTHORIZED, failedLogin.statusCode());
    }

    @Test
    @DisplayName("Обновление имени с авторизацией")
    public void updateNameWithAuth() {
        User randomUser = User.getRandomUser();
        String newName = randomUser.getName();
        User updateData = new User(null, null, newName);
        Response response = userApi.updateUserData(accessToken, updateData);
        assertEquals("Неверный код ответа при обновлении имени",
                SC_OK, response.statusCode());
        assertEquals("Имя не обновилось",
                newName, response.jsonPath().getString("user.name"));
    }

    @Test
    @DisplayName("Обновление нескольких полей одновременно")
    public void updateMultipleFieldsWithAuth() {
        User randomData = User.getRandomUser();
        String newEmail = randomData.getEmail();
        String newName = randomData.getName();
        String newPassword = randomData.getPassword();
        User updateData = new User(newEmail, newPassword, newName);
        Response updateResponse = userApi.updateUserData(accessToken, updateData);
        assertEquals("При обновлении данных пользователя должен возвращаться статус 200 OK",
                SC_OK, updateResponse.statusCode());
        Response getUserResponse = userApi.getUserData(accessToken);
        String actualEmail = getUserResponse.jsonPath().getString("user.email");
        assertEquals("Email пользователя не обновился",
                newEmail, actualEmail);

        String actualName = getUserResponse.jsonPath().getString("user.name");
        assertEquals("Имя пользователя не обновилось",
                newName, actualName);
        Response loginResponse = userApi.loginUser(new User(newEmail, newPassword, null));
        assertEquals("Не прошла авторизация с новым паролем",
                SC_OK, loginResponse.statusCode());
    }

    @Test
    @DisplayName("Обновление без авторизации")
    public void updateWithoutAuth() {
        User newData = User.getRandomUser();
        Response response = userApi.updateUserData("", newData);
        assertEquals("Без авторизации код ответа должен быть 401",
                SC_UNAUTHORIZED, response.statusCode());
        assertFalse("Success должен быть false",
                response.jsonPath().getBoolean("success"));
        assertEquals("Неверное сообщение об ошибке авторизации",
                AUTH_ERROR, response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Обновление на занятый email")
    public void updateToExistingEmail() {
        User anotherUser = User.getRandomUser();
        userApi.createUser(anotherUser);
        User invalidUpdate = new User(anotherUser.getEmail(), null, null);
        Response response = userApi.updateUserData(accessToken, invalidUpdate);
        assertEquals("Если передать почту, которая уже используется, вернётся код ответа 403",
                SC_FORBIDDEN, response.statusCode());
        assertEquals("Неверное сообщение об ошибке",
                EMAIL_ALREADY_EXISTS_MESSAGE, response.jsonPath().getString("message"));
    }

    @After
    public void tearDown() {
        userApi.deleteUser(accessToken);
    }
}