package api;

import io.qameta.allure.*;
import io.restassured.response.Response;
import java.util.*;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;

/**
 * Класс с методами API для ингредиентов.
 */
public class IngredientsApi {
    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site";
    private static final String INGREDIENTS = "/api/ingredients";

    @Step("Получение списка всех ингредиентов")
    public Response getAllIngredients() {
        return given()
                .baseUri(BASE_URL)
                .when()
                .get(INGREDIENTS);
    }
    @Step("Получение двух разных ингредиентов (bun и main/sauce)")
    public String[] getTwoIngredients() {
        Response response = getAllIngredients();
        response.then().statusCode(SC_OK);

        List<Map<String, String>> ingredients = response.jsonPath().getList("data");

        String bunId = ingredients.stream()
                .filter(i -> "bun".equals(i.get("type")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No bun ingredients found"))
                .get("_id");

        String otherId = ingredients.stream()
                .filter(i -> !"bun".equals(i.get("type")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No non-bun ingredients found"))
                .get("_id");

        return new String[]{bunId, otherId};
    }
}
