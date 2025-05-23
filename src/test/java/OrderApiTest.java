import api.*;
import io.qameta.allure.*;
import io.qameta.allure.junit4.*;
import io.restassured.response.Response;
import org.junit.*;
import static org.apache.http.HttpStatus.*;
import org.assertj.core.api.SoftAssertions;
import java.util.List;

import models.User;

/**
 * Проверки создания заказа и получение заказа конкретного пользователя
 */
public class OrderApiTest {
    private static final String ERROR_MESSAGE_NO_INGREDIENTS = "Ingredient ids must be provided";
    private static final String ERROR_MESSAGE_UNAUTHORIZED = "You should be authorised";

    private UserApi userApi;
    private OrderApi orderApi;
    private IngredientsApi ingredientsApi;
    private String[] ingredients;
    private User user;
    private String accessToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
        orderApi = new OrderApi();
        ingredientsApi = new IngredientsApi();
        ingredients = ingredientsApi.getTwoIngredients();

        user = User.getRandomUser();
        userApi.createUser(user);
        accessToken = userApi.getAccessToken(user);
    }

    @Test
    @DisplayName("Создание заказа без авторизации с ингредиентами")
    @Description("Система должна разрешать создание заказа")
    public void createOrderWithoutAuthWithIngredients() {
        accessToken = null;
        Response response = orderApi.createOrder(ingredients, null);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа")
                .isEqualTo(SC_OK);
        softly.assertThat((Boolean) response.path("success"))
                .as("Поле success должно быть true")
                .isTrue();
        softly.assertThat((Integer) response.path("order.number"))
                .as("Номер заказа должен присутствовать")
                .isNotNull();
        softly.assertAll();
    }

    @Test
    @DisplayName("Создание заказа без авторизации без ингредиентов")
    @Description("Система требует обязательного указания ингредиентов и не создает заказ")
    public void createOrderWithoutAuthWithoutIngredients() {
        accessToken = null;
        String[] emptyIngredients = new String[]{};
        Response response = orderApi.createOrder(emptyIngredients, null);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа")
                .isEqualTo(SC_BAD_REQUEST);
        softly.assertThat((Boolean) response.path("success"))
                .as("Поле success должно быть false")
                .isFalse();
        softly.assertThat((String) response.path("message"))
                .as("Неверное сообщение об ошибке")
                .isEqualTo(ERROR_MESSAGE_NO_INGREDIENTS);
        softly.assertAll();
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Система должна разрешать создание заказа")
    public void createOrderWithAuthWithIngredients() {
        Response response = orderApi.createOrder(ingredients, accessToken);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа")
                .isEqualTo(SC_OK);
        softly.assertThat((Boolean) response.path("success"))
                .as("Поле success должно быть true")
                .isTrue();
        softly.assertThat((Integer) response.path("order.number"))
                .as("Проверка что номер заказа не null")
                .isNotNull();
        softly.assertAll();
    }

    @Test
    @DisplayName("Создание заказа с авторизацией без ингредиентов")
    @Description("Система требует обязательного указания ингредиентов и не создает заказ")
    public void createOrderWithAuthWithoutIngredients() {
        String[] emptyIngredients = new String[]{};
        Response response = orderApi.createOrder(emptyIngredients, accessToken);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа")
                .isEqualTo(SC_BAD_REQUEST);
        softly.assertThat((Boolean) response.path("success"))
                .as("Поле success должно быть false")
                .isFalse();
        softly.assertThat((String) response.path("message"))
                .as("Неверное сообщение об ошибке")
                .isEqualTo(ERROR_MESSAGE_NO_INGREDIENTS);
        softly.assertAll();
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Ожидается ошибка")
    public void createOrderWithInvalidIngredientHash() {
        Response response = orderApi.createOrder(new String[]{"invalid_hash"}, accessToken);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа")
                .isEqualTo(SC_INTERNAL_SERVER_ERROR);
        softly.assertThat(response.getBody().asString())
                .as("Тело ответа об ошибке должно быть не пустым")
                .isNotEmpty();
        softly.assertAll();
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    @Description("Система успешно возвращает историю заказов")
    public void getOrdersForAuthorizedUser() {
        orderApi.createOrder(ingredients, accessToken);
        Response response = orderApi.getUserOrders(accessToken);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа")
                .isEqualTo(SC_OK);
        softly.assertThat((Boolean) response.path("success"))
                .as("Поле success должно быть true")
                .isTrue();
        softly.assertThat((List<?>) response.path("orders"))
                .as("Список заказов не должен быть пустым")
                .isNotEmpty();
        softly.assertAll();
    }

    @Test
    @DisplayName("Получение списка заказов неавторизованного пользователя")
    @Description("Система требует авторизацию")
    public void getOrdersForUnauthorizedUser() {
        accessToken = null;
        Response response = orderApi.getUserOrdersWithoutAuth();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.statusCode())
                .as("Неверный код ответа")
                .isEqualTo(SC_UNAUTHORIZED);
        softly.assertThat((Boolean) response.path("success"))
                .as("Поле success должно быть false")
                .isFalse();
        softly.assertThat((String) response.path("message"))
                .as("Неверное сообщение об ошибке")
                .isEqualTo(ERROR_MESSAGE_UNAUTHORIZED);
        softly.assertAll();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userApi.deleteUser(accessToken);
        }
    }
}