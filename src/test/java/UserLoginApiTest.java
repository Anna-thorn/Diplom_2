import api.UserApi;
import io.qameta.allure.junit4.*;
import io.restassured.response.Response;
import org.junit.*;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

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
    public void loginWithExistingUserSuccessfully() {
        Response response = userApi.loginUser(existingUser);
        assertEquals(SC_OK, response.statusCode());
        assertTrue(response.jsonPath().getBoolean("success"));
        assertNotNull("AccessToken должен быть в ответе",
                response.jsonPath().getString("accessToken"));
    }

    @Test
    @DisplayName("Авторизация с неверным email")
    public void loginWithWrongEmail() {
        User invalidUser = new User("wrong@test.com", existingUser.getPassword(), null);
        verifyFailedLogin(invalidUser);
    }

    @Test
    @DisplayName("Авторизация с неверным паролем")
    public void loginWithWrongPassword() {
        User invalidUser = new User(existingUser.getEmail(), "wrong-password", null);
        verifyFailedLogin(invalidUser);
    }

    @Test
    @DisplayName("Авторизация без email")
    public void loginWithoutEmail() {
        User invalidUser = new User(null, existingUser.getPassword(), null);
        verifyFailedLogin(invalidUser);
    }

    @Test
    @DisplayName("Авторизация без пароля")
    public void loginWithoutPassword() {
        User invalidUser = new User(existingUser.getEmail(), null, null);
        verifyFailedLogin(invalidUser);
    }

    // вспомогательный метод для проверки неудачной авторизации
    private void verifyFailedLogin(User invalidUser) {
        Response response = userApi.loginUser(invalidUser);
        assertEquals(SC_UNAUTHORIZED, response.statusCode());
        assertFalse(response.jsonPath().getBoolean("success"));
        assertEquals(INCORRECT_EMAIL_OR_PASSWORD, response.jsonPath().getString("message"));
    }

    @After
    public void tearDown() {
        userApi.deleteUser(accessToken);
    }
}