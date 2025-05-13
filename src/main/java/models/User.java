package models;

import java.util.UUID;

/**
 * Модель пользователя для API тестов.
 */
public class User {
    private String email;
    private String password;
    private String name;

    public User() {}

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {return email;}
    public String getPassword() {return password;}
    public String getName() {return name;}

    public void setEmail(String email) {this.email = email;}
    public void setPassword(String password) {this.password = password;}
    public void setName(String name) {this.name = name;}

    public static User getRandomUser() {
        String randomString = UUID.randomUUID().toString().substring(0, 8);
        return new User(
                "test-" + randomString + "@yandex.ru",
                "password-" + randomString,
                "User-" + randomString
        );
    }
}