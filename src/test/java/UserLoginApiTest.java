import io.qameta.allure.*;
import io.qameta.allure.junit4.*;
import io.restassured.response.Response;
import org.junit.*;
import static org.apache.http.HttpStatus.*;
import org.assertj.core.api.SoftAssertions;

import api.UserApi;
import models.User;

/**
 * Проверки авторизации (как успешные, так и ошибочные)
 */

public class UserLoginApiTest {
    private static final String INCORRECT_EMAIL_OR_PASSWORD = "email or password are incorrect";

    private UserApi userApi;
    private User existingUser;
    private String accessToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
        existingUser = User.getRandomUser();
        Response response = userApi.createUser(existingUser);
        accessToken = response.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Успешная авторизация")
    @Description("Система корректно обрабатывает валидные учетные данные")
    public void loginWithExistingUserSuccessfully() {
        Response response = userApi.loginUser(existingUser);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).isEqualTo(SC_OK);
        softly.assertThat(response.jsonPath().getBoolean("success")).isTrue();
        softly.assertThat(response.jsonPath().getString("accessToken"))
                .describedAs("AccessToken должен быть в ответе")
                .isNotNull();
        softly.assertAll();
    }

    @Test
    @DisplayName("Авторизация с неверным email")
    @Description("Система выдает ошибку при вводе несуществующего email")
    public void loginWithWrongEmail() {
        User invalidUser = new User("wrong@test.com", existingUser.getPassword(), null);
        verifyFailedLogin(invalidUser);
    }

    @Test
    @DisplayName("Авторизация с неверным паролем")
    @Description("Система выдает ошибку при использовании неверного пароля")
    public void loginWithWrongPassword() {
        User invalidUser = new User(existingUser.getEmail(), "wrong-password", null);
        verifyFailedLogin(invalidUser);
    }

    @Test
    @DisplayName("Авторизация без email")
    @Description("Система выдает ошибку при отсутствии email")
    public void loginWithoutEmail() {
        User invalidUser = new User(null, existingUser.getPassword(), null);
        verifyFailedLogin(invalidUser);
    }

    @Test
    @DisplayName("Авторизация без пароля")
    @Description("Система выдает ошибку при отсутствии пароля")
    public void loginWithoutPassword() {
        User invalidUser = new User(existingUser.getEmail(), null, null);
        verifyFailedLogin(invalidUser);
    }

    // вспомогательный метод для проверки неудачной авторизации
    @Step("Проверка ответа при неудачной попытки авторизации")
    private void verifyFailedLogin(User invalidUser) {
        Response response = userApi.loginUser(invalidUser);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode()).isEqualTo(SC_UNAUTHORIZED);
        softly.assertThat(response.jsonPath().getBoolean("success")).isFalse();
        softly.assertThat(response.jsonPath().getString("message"))
                .isEqualTo(INCORRECT_EMAIL_OR_PASSWORD);
        softly.assertAll();
    }

    @After
    public void tearDown() {
        userApi.deleteUser(accessToken);
    }
}