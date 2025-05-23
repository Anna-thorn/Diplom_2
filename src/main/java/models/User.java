package models;

import lombok.*;
import net.datafaker.Faker;

/**
 * Модель пользователя для API тестов.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String email;
    private String password;
    private String name;

    private static final Faker faker = new Faker();

    public static User getRandomUser() {
        String randomString = faker.regexify("[a-z0-9]{8}");
        return new User(
                "test-" + randomString + "@yandex.ru",
                "password-" + randomString,
                faker.name().firstName() + "-" + randomString
        );
    }
}