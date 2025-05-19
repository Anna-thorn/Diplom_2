package api;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.qameta.allure.restassured.AllureRestAssured;
import static io.restassured.RestAssured.given;

public class BaseApi {
    private static final String BASE_URL = "https://stellarburgers.nomoreparties.site";

    protected static RequestSpecification getBaseRequestSpec() {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON);
    }
}