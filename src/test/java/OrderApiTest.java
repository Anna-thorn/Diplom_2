import api.*;
import io.qameta.allure.*;
import io.qameta.allure.junit4.*;
import io.restassured.response.Response;
import org.junit.*;
import static org.hamcrest.Matchers.*;
import static org.apache.http.HttpStatus.*;

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
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации без ингредиентов")
    @Description("Система требует обязательного указания ингредиентов и не создает заказ")
    public void createOrderWithoutAuthWithoutIngredients() {
        accessToken = null;
        Response response = orderApi.createOrder(new String[]{}, null);
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo(ERROR_MESSAGE_NO_INGREDIENTS));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Система должна разрешать создание заказа")
    public void createOrderWithAuthWithIngredients() {
        Response response = orderApi.createOrder(ingredients, accessToken);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с авторизацией без ингредиентов")
    @Description("Система требует обязательного указания ингредиентов и не создает заказ")
    public void createOrderWithAuthWithoutIngredients() {
        Response response = orderApi.createOrder(new String[]{}, accessToken);
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo(ERROR_MESSAGE_NO_INGREDIENTS));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Ожидается ошибка")
    public void createOrderWithInvalidIngredientHash() {
        Response response = orderApi.createOrder(new String[]{"invalid_ingredient_hash_123"}, accessToken);
        response.then()
                .statusCode(SC_INTERNAL_SERVER_ERROR)
                .body(anything());
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя")
    @Description("Система успешно возвращает историю заказов")
    public void getOrdersForAuthorizedUser() {
        orderApi.createOrder(ingredients, accessToken);
        Response response = orderApi.getUserOrders(accessToken);
        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("orders", not(empty()));
    }

    @Test
    @DisplayName("Получение списка заказов неавторизованного пользователя")
    @Description("Система требует авторизацию")
    public void getOrdersForUnauthorizedUser() {
        accessToken = null;
        Response response = orderApi.getUserOrdersWithoutAuth();
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo(ERROR_MESSAGE_UNAUTHORIZED));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            userApi.deleteUser(accessToken);
        }
    }
}