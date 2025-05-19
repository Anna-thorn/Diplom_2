import io.qameta.allure.*;
import io.qameta.allure.junit4.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.*;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

import models.User;
import api.UserApi;

/**
 * Проверки создания и удаления пользователя
 */

public class UserCreationApiTest {
    private static final String USER_EXISTS = "User already exists";
    private static final String REQUIRED_FIELDS = "Email, password and name are required fields";

    private UserApi userApi;
    private User testUser;
    private String accessToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
    }

    @Test
    @DisplayName("Успешное создание пользователя (все поля заполнены)")
    @Description("Проверка успешного создания пользователя при корректном заполнении всех обязательных полей")
    public void createUserWithValidData() {
        testUser = User.getRandomUser();
        Response response = userApi.createUser(testUser);
        assertEquals(SC_OK, response.statusCode());
        verifySuccessResponse(response, testUser);
        accessToken = response.jsonPath().getString("accessToken");
    }

    @Test
    @DisplayName("Создание уже существующего пользователя")
    @Description("Проверка обработки попытки создания пользователя с уже существующими учетными данными")
    public void createDuplicateUser() {
        testUser = User.getRandomUser();
        userApi.createUser(testUser);
        Response response = userApi.createUser(testUser);
        assertEquals(SC_FORBIDDEN, response.statusCode());
        verifyErrorResponse(response, USER_EXISTS);
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Проверка обработки попытки создания пользователя без указания email, но с корректным именем и паролем")
    public void createUserWithoutEmail() {
        testUser = new User(null, "password", "Name");
        verifyRequiredFieldsError(testUser);
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Проверка обработки попытки создания пользователя без указания пароля, но с корректным именем и email")
    public void createUserWithoutPassword() {
        testUser = new User("email@test.com", null, "Name");
        verifyRequiredFieldsError(testUser);
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Проверка обработки попытки создания пользователя без указания имени, но с корректным паролем и email")
    public void createUserWithoutName() {
        testUser = new User("email@test.com", "password", null);
        verifyRequiredFieldsError(testUser);
    }

    // вспомогательные методы
    @Step("Проверка успешного ответа при создании пользователя")
    private void verifySuccessResponse(Response response, User user) {
        JsonPath json = response.jsonPath();
        assertTrue(json.getBoolean("success"));
        assertEquals(user.getEmail(), json.getString("user.email"));
        assertEquals(user.getName(), json.getString("user.name"));
        assertNotNull(json.getString("accessToken"));
        assertNotNull(json.getString("refreshToken"));
    }

    @Step("Проверка ответа с ошибкой: {expectedMessage}")
    private void verifyErrorResponse(Response response, String expectedMessage) {
        JsonPath json = response.jsonPath();
        assertFalse(json.getBoolean("success"));
        assertEquals(expectedMessage, json.getString("message"));
    }

    @Step("Проверка ошибки обязательных полей для пользователя")
    private void verifyRequiredFieldsError(User invalidUser) {
        Response response = userApi.createUser(invalidUser);
        assertEquals(SC_FORBIDDEN, response.statusCode());
        verifyErrorResponse(response, REQUIRED_FIELDS);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            Response response = userApi.deleteUser(accessToken);
            assertEquals(SC_ACCEPTED, response.statusCode());
            assertTrue(response.jsonPath().getBoolean("success"));
        }
    }
}