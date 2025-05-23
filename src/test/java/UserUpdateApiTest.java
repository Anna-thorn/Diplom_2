import io.qameta.allure.junit4.*;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.*;
import static org.apache.http.HttpStatus.*;
import org.assertj.core.api.SoftAssertions;

import api.UserApi;
import models.User;

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
    @DisplayName("Обновление email")
    @Description("Проверка успешного обновления email авторизованного пользователя")
    public void updateEmailWithAuth() {
        String newEmail = User.getRandomUser().getEmail();
        User updateData = new User(newEmail, null, null);
        Response response = userApi.updateUserData(accessToken, updateData);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа при обновлении email")
                .isEqualTo(SC_OK);
        softly.assertThat(response.jsonPath().getString("user.email"))
                .as("Email не обновился")
                .isEqualTo(newEmail);
        softly.assertAll();
    }

    @Test
    @DisplayName("Обновление пароля")
    @Description("Проверка смены пароля и последующей авторизации")
    public void updatePasswordWithAuth() {
        User randomUser = User.getRandomUser();
        String newPassword = randomUser.getPassword();
        User updateData = new User(null, newPassword, null);
        Response updateResponse = userApi.updateUserData(accessToken, updateData);
        User newCredentials = new User(user.getEmail(), newPassword, null);
        Response loginResponse = userApi.loginUser(newCredentials);
        Response failedLogin = userApi.loginUser(user);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(updateResponse.statusCode())
                .as("Неверный код ответа при обновлении пароля")
                .isEqualTo(SC_OK);
        softly.assertThat(loginResponse.statusCode())
                .as("Вход с новым паролем не удался")
                .isEqualTo(SC_OK);
        softly.assertThat(failedLogin.statusCode())
                .as("Вход со старым паролем должен возвращать 401")
                .isEqualTo(SC_UNAUTHORIZED);
        softly.assertAll();
    }

    @Test
    @DisplayName("Обновление имени")
    @Description("Проверка изменения имени пользователя")
    public void updateNameWithAuth() {
        User randomUser = User.getRandomUser();
        String newName = randomUser.getName();
        User updateData = new User(null, null, newName);
        Response response = userApi.updateUserData(accessToken, updateData);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа при обновлении имени")
                .isEqualTo(SC_OK);
        softly.assertThat(response.jsonPath().getString("user.name"))
                .as("Имя не обновилось")
                .isEqualTo(newName);
        softly.assertAll();
    }

    @Test
    @DisplayName("Обновление нескольких полей")
    @Description("Комплексная проверка одновременного обновления email, пароля и имени")
    public void updateMultipleFieldsWithAuth() {
        User randomData = User.getRandomUser();
        String newEmail = randomData.getEmail();
        String newName = randomData.getName();
        String newPassword = randomData.getPassword();
        User updateData = new User(newEmail, newPassword, newName);
        Response updateResponse = userApi.updateUserData(accessToken, updateData);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(updateResponse.statusCode())
                .as("При обновлении данных пользователя должен возвращаться статус 200 OK")
                .isEqualTo(SC_OK);
        Response getUserResponse = userApi.getUserData(accessToken);
        softly.assertThat(getUserResponse.jsonPath().getString("user.email"))
                .as("Email пользователя не обновился")
                .isEqualTo(newEmail);
        softly.assertThat(getUserResponse.jsonPath().getString("user.name"))
                .as("Имя пользователя не обновилось")
                .isEqualTo(newName);
        Response loginResponse = userApi.loginUser(new User(newEmail, newPassword, null));
        softly.assertThat(loginResponse.statusCode())
                .as("Не прошла авторизация с новым паролем")
                .isEqualTo(SC_OK);
        softly.assertAll();
    }

    @Test
    @DisplayName("Обновление без авторизации")
    @Description("Проверка защиты от неавторизованных запросов")
    public void updateWithoutAuth() {
        User newData = User.getRandomUser();
        Response response = userApi.updateUserData("", newData);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Без авторизации код ответа должен быть 401")
                .isEqualTo(SC_UNAUTHORIZED);
        softly.assertThat(response.jsonPath().getBoolean("success"))
                .as("Поле success должно быть false")
                .isFalse();
        softly.assertThat(response.jsonPath().getString("message"))
                .as("Неверное сообщение об ошибке авторизации")
                .isEqualTo(AUTH_ERROR);
        softly.assertAll();
    }

    @Test
    @DisplayName("Обновление на занятый email")
    @Description("Проверка валидации уникальности email при обновлении данных")
    public void updateToExistingEmail() {
        User anotherUser = User.getRandomUser();
        userApi.createUser(anotherUser);
        User invalidUpdate = new User(anotherUser.getEmail(), null, null);
        Response response = userApi.updateUserData(accessToken, invalidUpdate);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("При использовании существующего email вернётся код 403")
                .isEqualTo(SC_FORBIDDEN);
        softly.assertThat(response.jsonPath().getString("message"))
                .as("Неверное сообщение об ошибке")
                .isEqualTo(EMAIL_ALREADY_EXISTS_MESSAGE);
        softly.assertAll();
    }

    @After
    public void tearDown() {
        userApi.deleteUser(accessToken);
    }
}